package fr.ifremer.sensornanny.sync.processor;

import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;
import fr.ifremer.sensornanny.sync.parse.Extensions;
import fr.ifremer.sensornanny.sync.processor.impl.ObservationDelegateProcessorImpl;

/**
 * Elastic Processor
 * 
 * @author athorel
 *
 */
public class ElasticProcessorImpl implements IElasticProcessor {

    private static final Logger LOGGER = Logger.getLogger(ElasticProcessorImpl.class.getName());

    private static final String FILE_DELETED = "file_deleted";
    private static final String FILE_CREATED = "file_created";

    @Inject
    ObservationDelegateProcessorImpl observationDeleteProcessor;

    public Runnable of(final FileInfo fileInfo, final List<Activity> activities) {
        return new Runnable() {
            @Override
            public void run() {
                String tName = Thread.currentThread().getName();
                Long fileId = fileInfo.getFileId();
                String filePath = fileInfo.getFilePath();
                LOGGER.info(String.format("[%s] Process file {id:%d,name:%s}", tName, fileId, filePath));
                boolean isCreated = false;
                boolean isDeleted = false;
                for (Activity activity : activities) {
                    isCreated |= FILE_CREATED.equals(activity.getType());
                    isDeleted |= FILE_DELETED.equals(activity.getType()) || activity.getFilePath().contains("trash");
                }

                // Don't index
                if (isCreated && isDeleted) {
                    LOGGER.info(String.format("[%s] File has deletion and creation, file won't be indexed", tName));
                    return;
                }

                IDelegateProcessor delegateProcessor = getDelegateProcessor(fileInfo);
                if (delegateProcessor != null) {
                    delegateProcessor.execute(fileInfo, isDeleted);
                } else {
                    LOGGER.info(String.format("[%s] No processor found for file {id: %d, name: %s}", tName, fileId,
                            filePath));
                }

            }
        };
    }

    private IDelegateProcessor getDelegateProcessor(FileInfo fileInfo) {
        String filePath = fileInfo.getFilePath();
        if (Extensions.SENSOR_ML.accept(filePath)) {
            return null;
        } else if (Extensions.XML.accept(filePath)) {
            return observationDeleteProcessor;
        }
        return null;
    }

}
