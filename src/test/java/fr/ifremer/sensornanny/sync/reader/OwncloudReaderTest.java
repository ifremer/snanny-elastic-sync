package fr.ifremer.sensornanny.sync.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.MockTest;
import fr.ifremer.sensornanny.sync.builder.ActivityBuilder;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;
import fr.ifremer.sensornanny.sync.reader.impl.OwncloudReaderImpl;

public class OwncloudReaderTest extends MockTest {

    @TestSubject
    private OwncloudReaderImpl reader = new OwncloudReaderImpl();

    @Mock
    private IOwncloudDao dao;

    @Test
    public void testGetFilesEmpty() {
        Date from = new Date();
        Date to = new Date();

        // Register phase
        expect(dao.getActivities(from, to)).andReturn(new ArrayList<>());
        replayAll();

        // Play phase
        Map<FileInfo, List<Activity>> result = reader.getActivities(from, to);
        Assert.assertNotNull("result must not be null", result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetFilesWithTwoActivitiesOnSameId() {
        Date from = new Date();
        Date to = new Date();

        List<Activity> asList = Arrays.asList(ActivityBuilder.simpleActivity(1L, "file1.moe", "file_created", "admin"),
                ActivityBuilder.simpleActivity(1L, "file1.moe", "file_modified", "admin"));

        // Register phase
        expect(dao.getActivities(from, to)).andReturn(asList);
        replayAll();

        // Play phase
        Map<FileInfo, List<Activity>> result = reader.getActivities(from, to);
        Assert.assertNotNull("result must not be null", result);
        Assert.assertEquals(1, result.size());
        List<Activity> content = result.get(fileInfoOf(1L));
        Assert.assertFalse(content.isEmpty());
        Assert.assertEquals(2, content.size());
    }

    @Test
    public void testGetFilesWithTwoActivitiesOnDifferentId() {
        Date from = new Date();
        Date to = new Date();

        List<Activity> asList = Arrays.asList(ActivityBuilder.simpleActivity(1L, "file1.moe", "file_created", "admin"),
                ActivityBuilder.simpleActivity(2L, "file2.moe", "file_deleted", "admin"));

        // Register phase
        expect(dao.getActivities(from, to)).andReturn(asList);
        replayAll();

        // Play phase
        Map<FileInfo, List<Activity>> result = reader.getActivities(from, to);
        Assert.assertNotNull("result must not be null", result);
        Assert.assertEquals(2, result.size());
        List<Activity> content = result.get(fileInfoOf(1L));
        Assert.assertFalse(content.isEmpty());
        Assert.assertEquals(1, content.size());
        content = result.get(fileInfoOf(2L));
        Assert.assertFalse(content.isEmpty());
        Assert.assertEquals(1, content.size());
    }

    private FileInfo fileInfoOf(Long id) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(id);
        return fileInfo;
    }
}
