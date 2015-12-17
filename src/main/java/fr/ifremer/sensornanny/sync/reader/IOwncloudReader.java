package fr.ifremer.sensornanny.sync.reader;

import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;

public interface IOwncloudReader {

    /**
     * Retrieve the files which moves on a period
     * 
     * @param from the query start time
     * @param to the query end time
     * @return map containing each activity (creation, modification, deletion) for a file
     */
    Map<FileInfo, List<Activity>> getActivities(Date from, Date to);

    /**
     * Retrieve content form id
     * 
     * @param id identifier of the content
     * @return content element
     */
    Content getContent(Long id);

}
