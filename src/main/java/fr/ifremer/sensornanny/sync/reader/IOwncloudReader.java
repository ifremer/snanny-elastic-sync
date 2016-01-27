package fr.ifremer.sensornanny.sync.reader;

import java.util.Date;
import java.util.List;

import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

public interface IOwncloudReader {

    /**
     * Retrieve the files which moves on a period
     * 
     * @param from the query start time
     * @param to the query end time
     * @return map containing each activity (creation, modification, deletion) for a file
     */
    List<OwncloudSyncModel> getActivities(Date from, Date to);

    /**
     * Retrieve the files which failed during the last sync
     * 
     * @return map containing each activity for a file
     */
    List<OwncloudSyncModel> getFailedSyncActivities();

    /**
     * Retrieve content form id
     * 
     * @param uuid identifier of the O&M
     * @return content element
     */
    Content getOMContent(String uuid);

}
