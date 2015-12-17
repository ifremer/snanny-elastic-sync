package fr.ifremer.sensornanny.sync.processor;

import java.util.List;

import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;

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
     * @param activities list of changes on the file
     * @return Runnable of processor
     */
    Runnable of(final FileInfo fileInfo, final List<Activity> activities);
}
