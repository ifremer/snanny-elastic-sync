package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.springframework.web.client.HttpClientErrorException;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.observation.parser.IObservationParser;
import fr.ifremer.sensornanny.observation.parser.ObservationData;
import fr.ifremer.sensornanny.observation.parser.TimePosition;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;
import fr.ifremer.sensornanny.sync.dto.model.OMResult;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.parse.ParserManager;

public class ObservationDataManager {

    public static final int ONE_MEGA_OCTECT_IN_OCTET = 1000000;

    @Inject
    private ParserManager parserManager;

    @Inject
    private IOwncloudDao owncloudDao;

    private Semaphore semaphore = new Semaphore(Config.maxMemory());

    public void readData(String uuid, OMResult observationResult, Consumer<TimePosition> consumer)
            throws DataNotFoundException {
        if(observationResult.getUrl() != null) {
            String fileName = new File(observationResult.getUrl()).getName();
            ObservationData data = ObservationData.of(fileName, observationResult.getRole());
            IObservationParser parser = parserManager.getParser(data);
            if (parser == null) {
                return;
            }

            Integer moduloForParser = Config.moduloForParser(parser.getClass());
            FileSizeInfo fileSize = getFileSize(uuid, observationResult.getUrl());
            if (fileSize != null && fileSize.getFileSize() != null) {
                Long fileInMo = fileSize.getFileSize() / ONE_MEGA_OCTECT_IN_OCTET;
                int permits = fileInMo.intValue();
                try {
                    // Acquire size elements
                    acquire(permits);
                    InputStream stream = owncloudDao.getResultData(uuid);
                    if (stream != null) {
                        parser.read(data, stream, new Consumer<TimePosition>() {

                            @Override
                            public void accept(TimePosition result) {
                                // Filter using modulo on the data manager
                                if (result.getRecordNumber() % moduloForParser == 0) {
                                    consumer.accept(result);
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (DataNotFoundException e) {
                    throw e;
                } finally {
                    release(permits);
                }
            } else {
                consumer.accept(null);
            }
        } else {
            consumer.accept(null);
        }
    }

    private FileSizeInfo getFileSize(String uuid, String filePath) throws DataNotFoundException {
        try {
            return owncloudDao.getResultFileSize(uuid);
        } catch (HttpClientErrorException e) {
            return null;
            //throw new DataNotFoundException("Unable to find result file " + filePath + " for uuid " + uuid);
        }
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
