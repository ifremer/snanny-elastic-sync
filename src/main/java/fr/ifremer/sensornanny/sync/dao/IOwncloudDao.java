package fr.ifremer.sensornanny.sync.dao;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

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
     * @param id identifier of the content
     * @return content element
     */
    Content getContent(Long id);

    /**
     * Retrieve the sml content from uuid
     * 
     * @param uuid uuid of the system
     * @return element from the system
     */
    String getSML(String uuid);

    /**
     * Retrieve the result file from an OM
     * 
     * @param idOM identifier of the OM
     * @param resultFileName result file name expected
     * @return inputstream which allow to read element
     */
    InputStream getResultData(Long idOM, String resultFileName) throws DataNotFoundException;

    /**
     * Retrieve the file informations from an idOM and a resultfileName
     * 
     * @return file information containing filesize
     */
    FileSizeInfo getFileSize(Long idOM, String resultFileName);

    /**
     * Retrieve the ancestors of a system
     * 
     * @param uuid system UUID
     * @return list of ancestors, otherwise <code>null</code>
     */
    List<String> getAncestors(String uuid);

    /**
     * Update indexation status to owncloud as feedback information to the owncloud user
     * 
     * @param indexStatus index status
     */
    void updateIndexStatus(IndexStatus indexStatus);
}
