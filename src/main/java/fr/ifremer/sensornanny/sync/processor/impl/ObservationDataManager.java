package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;
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
import fr.ifremer.sensornanny.sync.manager.DataFileManager;
import fr.ifremer.sensornanny.sync.parse.ParserManager;

public class ObservationDataManager {

    protected static final int ONE_MEGA_OCTECT_IN_OCTET = 1000000;

    private static final Logger LOGGER = Logger.getLogger(ObservationDataManager.class.getName());

    @Inject
    private ParserManager parserManager;

    @Inject
    private IOwncloudDao owncloudDao;

    @Inject
    private DataFileManager dataFileManager;

    private Semaphore semaphore = new Semaphore(Config.maxMemory());

    /**
     *
     * @param uuid
     * @param observationResult
     * @param consumer
     * @return has data
     * @throws DataNotFoundException
     */
    public boolean readData(String uuid, OMResult observationResult, Consumer<TimePosition> consumer)
            throws DataNotFoundException {

        boolean hasData = false;
        if (observationResult.getUrl() != null) {
            String fileName = new File(observationResult.getUrl()).getName();

            ObservationData data = ObservationData.of(fileName, observationResult.getRole());
            IObservationParser parser = parserManager.getParser(data);
            if (parser == null) {
                return false;
            }

            Integer moduloForParser = Config.moduloForParser(parser.getClass());
            FileSizeInfo fileSize = getFileSize(uuid);
            if (fileSize != null && fileSize.getFileSize() != null) {
                Long fileInMo = fileSize.getFileSize() / ONE_MEGA_OCTECT_IN_OCTET;
                int permits = fileInMo.intValue();
                //look for the good reader (archive or not)
                try(InputStream fileStream = dataFileManager.getReader(fileName).getFile(uuid)) {
                    // Acquire size elements
                    acquire(permits);
                    if (fileStream != null) {
                        hasData = true;
                        parser.read(data, fileStream,
                                (TimePosition result) -> {
                                    if (result.getRecordNumber() % moduloForParser == 0) {
                                        consumer.accept(result);
                                    }
                                }
                        );
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("Observation reader thread was interrupted");
                } catch (DataNotFoundException e) {
                    LOGGER.warning("File linked to id "+uuid+" is not found");
                    throw e;
                }  catch (IOException ioe){
                    LOGGER.log(Level.SEVERE, "");
                }
                finally {
                    release(permits);
                }
            } else {
                consumer.accept(null);
            }
        } else {
            consumer.accept(null);
        }

        return hasData;
    }

    private FileSizeInfo getFileSize(String uuid) throws DataNotFoundException {
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
