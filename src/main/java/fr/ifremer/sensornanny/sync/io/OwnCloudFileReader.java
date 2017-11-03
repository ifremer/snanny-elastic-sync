package fr.ifremer.sensornanny.sync.io;

import java.io.InputStream;
import org.springframework.util.Assert;
import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;

/**
 * Standard file reader used for any generic file format.
 * It recovers the inputstream directly from the Owncloud API
 * @author gpagniez
 */
public class OwnCloudFileReader implements DataFileReader{

    @Inject
    private IOwncloudDao owncloudDao;

    public InputStream getFile(String uuid) throws DataNotFoundException {
        Assert.hasText("uuid", "file id should not be empty");

        return owncloudDao.getResultData(uuid);
    }
}
