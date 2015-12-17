package fr.ifremer.sensornanny.sync.processor;

import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;

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
     * @param isDeleted <code>true</code> if the file is deleted otherwise <code>false</code>
     */
    void execute(FileInfo fileInfo, boolean isDeleted);
}
