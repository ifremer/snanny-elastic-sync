package fr.ifremer.sensornanny.sync.processor;

import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

/**
 * Delegate processor of elastic processors
 * 
 * @author athorel
 *
 */
public interface IDelegateProcessor {

    /**
     * Execute the treatment of the file
     * 
     * @param fileInfo file to handle
     */
    void execute(OwncloudSyncModel model);
}
