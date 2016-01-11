package fr.ifremer.sensornanny.sync.processor;

import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

/**
 * Interface that allow to process observatories
 * 
 * @author athorel
 *
 */
public interface IElasticProcessor {

    /**
     * Get runnable for execution
     * 
     * @param fileInfo description of a file
     * @return Runnable of processor
     */
    Runnable of(OwncloudSyncModel fileInfo);
}
