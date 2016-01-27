package fr.ifremer.sensornanny.sync.reader.impl;

import java.util.Date;
import java.util.List;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
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
    public List<OwncloudSyncModel> getActivities(Date from, Date to) {
        return owncloudDao.getActivities(from, to);
    }

    @Override
    public List<OwncloudSyncModel> getFailedSyncActivities() {
        return owncloudDao.getFailedActivities();
    }

    @Override
    public Content getOMContent(String uuid) {
        return owncloudDao.getOM(uuid);
    }

}
