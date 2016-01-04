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

import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.dao.impl.OwncloudDaoImpl;
import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;

public class OwncloudDaoTest extends UnitTest {

    public OwncloudDaoImpl dao = new OwncloudDaoImpl();

    @Test
    public void testGetFiles() {
        Calendar instance = Calendar.getInstance();
        Date to = instance.getTime();
        instance.add(Calendar.MONTH, -2);

        List<Activity> result = dao.getActivities(instance.getTime(), to);
        for (Activity activity : result) {
            System.out.println(activity);
        }
    }

    @Test
    public void testGetFilesDAYS() {
        Calendar instance = Calendar.getInstance();
        Date to = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -2);

        List<Activity> result = dao.getActivities(instance.getTime(), to);
        for (Activity activity : result) {
            System.out.println(activity);
        }
    }

    @Test
    public void testGetContent() {
        Content result = dao.getContent(33L);
        System.out.println(result.getPath());
    }

    @SuppressWarnings("resource")
    @Test
    public void testCSVParserGetContent() throws IOException {
        InputStream result = dao.getResultData(670L, "turb2014.csv");
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
