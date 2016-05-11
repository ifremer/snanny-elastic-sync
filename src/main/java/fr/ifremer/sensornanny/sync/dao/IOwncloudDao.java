package fr.ifremer.sensornanny.sync.dao;

import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Owncloud interface for data-access
 * 
 * @author athorel
 *
 */
public interface IOwncloudDao {

    /**
     * Retrieve the files which moves on a period
     * 
     * @param from the query start time
     * @param to the query end time
     * @return list containing files created/edited/deleted/shared/unshared between the two dates
     */
    List<OwncloudSyncModel> getActivities(Date from, Date to);

    /**
     * Retrieve the last failures items
     * 
     * @return list containing each O&M files which failed during the last synchronisation
     */
    List<OwncloudSyncModel> getFailedActivities();

    /**
     * Retrieve content form id
     * 
     * @param uuid identifier of the content
     * @return content element
     */
    Content getOM(String uuid);

    /**
     * Retrieve the sml content from uuid
     * 
     * @param uuid uuid of the system
     * @return element from the system
     */
    String getSML(String uuid);

    /**
     * Retrieve the sml content from uuid
     *
     * @param uuid uuid of the system
     * @param startTime start
     * @param endTime end
     * @return element from the system
     */
    String getSML(String uuid, Date startTime, Date endTime);

    /**
     * Retrieve the result file from an OM
     * 
     * @param uuid unique identifier of the OM
     * @return inputstream which allow to read element
     */
    InputStream getResultData(String uuid) throws DataNotFoundException;

    /**
     * Retrieve the file informations from an uuid observations
     * 
     * @param uuid unique identifier of the OM
     * @return file information containing filesize
     */
    FileSizeInfo getResultFileSize(String uuid);

    /**
     * Retrieve the ancestors of a system
     * 
     * @param uuid system UUID
     * @param beginPosition : date de début de l'observation
     * @param endPosition : date de fin de l'observation
     * @return list of ancestors, otherwise <code>null</code>
     */
    List<String> getAncestors(String uuid, Date beginPosition, Date endPosition);

    /**
     * Update indexation status to owncloud as feedback information to the owncloud user
     * 
     * @param indexStatus index status
     */
    void updateIndexStatus(IndexStatus indexStatus);
}
