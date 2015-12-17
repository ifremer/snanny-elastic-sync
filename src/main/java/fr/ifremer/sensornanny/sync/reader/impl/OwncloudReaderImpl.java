package fr.ifremer.sensornanny.sync.reader.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;
import fr.ifremer.sensornanny.sync.reader.IOwncloudReader;

/**
 * Concrete implementation of owncloud reader
 * 
 * @author athorel
 *
 */
public class OwncloudReaderImpl implements IOwncloudReader {

    @Inject
    private IOwncloudDao owncloudDao;

    @Override
    public Map<FileInfo, List<Activity>> getActivities(Date from, Date to) {
        List<Activity> activities = owncloudDao.getActivities(from, to);
        Map<FileInfo, List<Activity>> map = new HashMap<>();

        for (Activity activity : activities) {
            Long fileId = activity.getFileId();
            FileInfo info = new FileInfo();
            info.setFileId(fileId);
            info.setFilePath(activity.getFilePath());

            List<Activity> list = map.get(info);
            if (list == null) {
                list = new ArrayList<Activity>();
                map.put(info, list);
            }
            list.add(activity);
        }
        return map;
    }

    @Override
    public Content getContent(Long id) {
        return owncloudDao.getContent(id);
    }

}
