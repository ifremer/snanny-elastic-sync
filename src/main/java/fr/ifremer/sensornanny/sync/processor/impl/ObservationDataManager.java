package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.model.OMResult;
import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.parse.observations.IObservationParser;
import fr.ifremer.sensornanny.sync.parse.observations.impl.MomarObservationParser;
import fr.ifremer.sensornanny.sync.parse.observations.impl.NetCdfObservationParser;

public class ObservationDataManager {

    public static final int ONE_MEGA_OCTECT_IN_OCTET = 1000000;

    @Inject
    private MomarObservationParser momarParser;

    @Inject
    private NetCdfObservationParser netCdfParser;

    @Inject
    private IOwncloudDao owncloudDao;

    private Semaphore semaphore = new Semaphore(Config.maxMemory());

    public void readData(Long omId, OMResult observationResult, Consumer<TimePosition> consumer) {
        IObservationParser<TimePosition> parser = getParser(observationResult.getRole());
        if (parser == null) {
            return;
        }

        String fileName = new File(observationResult.getUrl()).getName();
        FileSizeInfo fileSize = owncloudDao.getFileSize(omId, fileName);
        if (fileSize != null && fileSize.getFileSize() != null) {
            Long fileInMo = fileSize.getFileSize() / ONE_MEGA_OCTECT_IN_OCTET;
            int permits = fileInMo.intValue();
            try {
                // Acquire size elements
                acquire(permits);
                InputStream stream = owncloudDao.getResultData(omId, fileName);
                if (stream != null) {
                    parser.read(fileName, stream, consumer);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                release(permits);
            }
        }
    }

    private IObservationParser<TimePosition> getParser(String role) {
        if (momarParser.accept(role)) {
            return momarParser;
        }
        if (netCdfParser.accept(role)) {
            return netCdfParser;
        }
        return null;
    }

    protected void acquire(int permits) throws InterruptedException {
        semaphore.acquire(permits);
    }

    protected void release(int permits) {
        semaphore.release(permits);
    }

    protected int drainPermits() {
        return semaphore.drainPermits();
    }

}
