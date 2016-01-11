package fr.ifremer.sensornanny.sync.processor;

import java.util.logging.Logger;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
import fr.ifremer.sensornanny.sync.processor.impl.ObservationDelegateProcessorImpl;

/**
 * Elastic Processor
 * 
 * @author athorel
 *
 */
public class ElasticProcessorImpl implements IElasticProcessor {

    private static final Logger LOGGER = Logger.getLogger(ElasticProcessorImpl.class.getName());

    @Inject
    ObservationDelegateProcessorImpl observationDeleteProcessor;

    public Runnable of(final OwncloudSyncModel model) {
        return new Runnable() {
            @Override
            public void run() {
                String tName = Thread.currentThread().getName();
                Long fileId = model.getFileId();
                String filePath = model.getName();
                LOGGER.info(String.format("[%s] Process O&M {id:%d,name:%s}", tName, fileId, filePath));
                observationDeleteProcessor.execute(model);
            }
        };
    }

}
