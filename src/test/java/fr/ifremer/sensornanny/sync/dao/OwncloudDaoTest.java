package fr.ifremer.sensornanny.sync.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import fr.ifremer.sensornanny.sync.base.IntegrationTest;
import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.dao.impl.OwncloudDaoImpl;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

@Category(IntegrationTest.class)
public class OwncloudDaoTest extends UnitTest {

    public OwncloudDaoImpl dao = new OwncloudDaoImpl();

    @Test
    public void testGetFiles() {
        Calendar instance = Calendar.getInstance();
        Date to = instance.getTime();
        instance.add(Calendar.MONTH, -2);

        List<OwncloudSyncModel> result = dao.getActivities(instance.getTime(), to);
        for (OwncloudSyncModel activity : result) {
            System.out.println(activity);
        }
    }

    @Test
    public void testGetFilesDAYS() {
        Calendar instance = Calendar.getInstance();
        Date to = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -2);

        List<OwncloudSyncModel> result = dao.getActivities(instance.getTime(), to);
        for (OwncloudSyncModel activity : result) {
            System.out.println(activity);
        }
    }

    @Test
    public void testGetContent() {
        Content result = dao.getOM("53048b87-4225-4e92-8e87-c33a955c4928");
        System.out.println(result.getContent());
    }

    @SuppressWarnings("resource")
    @Test
    public void testCSVParserGetContent() throws IOException {
        FileSizeInfo resultFileSize = dao.getResultFileSize("53048b87-4225-4e92-8e87-c33a955c4928");
        System.out.println(resultFileSize.getFileName() + "  " + resultFileSize.getFileSize());
        InputStream result = dao.getResultData("53048b87-4225-4e92-8e87-c33a955c4928");
        CSVParser csvParser = new CSVParser(new InputStreamReader(result), CSVFormat.DEFAULT);
        csvParser.forEach(new Consumer<CSVRecord>() {

            @Override
            public void accept(CSVRecord t) {
                System.out.println(t);
            }
        });
    }

    @Test
    public void testGetAncestors() {
        List<String> ancestors = dao.getAncestors("7764633d-499b-4a17-bade-6059beabc229");
        Assert.assertNotNull("ancestors must not be null", ancestors);
        Assert.assertEquals(1, ancestors.size());
    }

    @Test
    public void testPostIndex() {
        IndexStatus idx = new IndexStatus();
        idx.setIndexedObservations(100);
        idx.setMessage("OK");
        idx.setStatus(true);
        idx.setTime(System.currentTimeMillis());
        idx.setUuid("abb037654-myuuid-sdsdgfqsdfwhbv");

        dao.updateIndexStatus(idx);
    }

}
