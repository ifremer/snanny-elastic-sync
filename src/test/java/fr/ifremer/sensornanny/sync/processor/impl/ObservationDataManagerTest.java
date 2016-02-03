package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.InputStream;
import java.util.function.Consumer;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import fr.ifremer.sensornanny.observation.parser.ObservationData;
import fr.ifremer.sensornanny.observation.parser.TimePosition;
import fr.ifremer.sensornanny.sync.base.MockTest;
import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.converter.PermissionsConverter;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;
import fr.ifremer.sensornanny.sync.dto.model.OMResult;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;

@Category(UnitTest.class)
public class ObservationDataManagerTest extends MockTest {

    @TestSubject
    ObservationDataManager manager = new ObservationDataManager();

    @Mock
    FakeParser parser;

    @Mock(type = MockType.NICE)
    PermissionsConverter permissionConverter;

    @Mock
    IOwncloudDao owncloudDao;

    @Test
    public void testManagerIT() throws InterruptedException, DataNotFoundException {
        Capture<Integer> capture = EasyMock.newCapture();
        OMResult omResult = new OMResult();
        String role = "application/netcdf";
        omResult.setRole(role);
        String fileName = "file1.nav";
        omResult.setUrl(fileName);

        ObservationData observation = ObservationData.of(fileName, role);

        FileSizeInfo value = new FileSizeInfo();
        int maxMemory = Config.maxMemory();
        // Acquire hal permits
        int initialPermit = maxMemory / 2;
        int fileInMO = initialPermit - 3;
        // Set Size is next all permits
        long i = fileInMO * ObservationDataManager.ONE_MEGA_OCTECT_IN_OCTET;
        value.setFileSize(Long.valueOf(i));

        expect(parser.accept(observation)).andReturn(true);
        String idOM = "1L";
        expect(owncloudDao.getResultFileSize(idOM)).andReturn(value);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("momar/obs/optode2011.csv");
        expect(owncloudDao.getResultData(idOM)).andReturn(resourceAsStream);

        Consumer<TimePosition> consumer = new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                capture.setValue(manager.drainPermits());
            }
        };

        parser.read(observation, resourceAsStream, consumer);
        expectLastCall().andStubDelegateTo(new FakeParser() {

            @Override
            public void read(ObservationData fileName, InputStream stream, Consumer<TimePosition> consumer) {
                sleep(100L);
                consumer.accept(new TimePosition());
            }

        });

        replayAll();
        new Thread(new Runnable() {

            @Override
            public void run() {
                safeAcquireAndRelease(initialPermit, 500);
            }

        }).start();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    manager.readData(idOM, omResult, consumer);
                } catch (DataNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Thread.sleep(800);
        Assert.assertEquals(Config.maxMemory() - initialPermit - fileInMO, capture.getValue().intValue());
    }

    @Test
    public void testManagerWaitIT() throws InterruptedException, DataNotFoundException {

        Capture<Integer> capture = EasyMock.newCapture();
        OMResult omResult = new OMResult();
        String role = "application/netcdf";
        omResult.setRole(role);
        String fileName = "file1.nav";
        omResult.setUrl(fileName);
        ObservationData observation = ObservationData.of(fileName, role);

        FileSizeInfo value = new FileSizeInfo();
        int maxMemory = Config.maxMemory();
        // Acquire hal permits
        final int permits = maxMemory / 2;

        int fileInMo = permits + 4;
        // Set Size is next all permits
        final long i = fileInMo * ObservationDataManager.ONE_MEGA_OCTECT_IN_OCTET;
        value.setFileSize(Long.valueOf(i));
        // expect(momarParser.accept(omResult.getRole())).andReturn(false);
        expect(parser.accept(observation)).andReturn(true);
        String idOM = "1L";
        expect(owncloudDao.getResultFileSize(idOM)).andReturn(value);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("momar/obs/optode2011.csv");
        expect(owncloudDao.getResultData(idOM)).andReturn(resourceAsStream);

        Consumer<TimePosition> consumer = new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                capture.setValue(manager.drainPermits());
            }
        };

        parser.read(observation, resourceAsStream, consumer);
        expectLastCall().andStubDelegateTo(new FakeParser() {

            @Override
            public void read(ObservationData data, InputStream stream, Consumer<TimePosition> consumer) {
                sleep(100L);
                consumer.accept(new TimePosition());
            }
        });
        replayAll();

        new Thread(new Runnable() {

            @Override
            public void run() {
                safeAcquireAndRelease(permits, 300);
            }
        }).start();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    manager.readData(idOM, omResult, consumer);
                } catch (DataNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Thread.sleep(500);
        Assert.assertEquals(Config.maxMemory() - fileInMo, capture.getValue().intValue());
    }

    private void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void safeAcquireAndRelease(int initialPermit, long time) {
        try {
            manager.acquire(initialPermit);
            sleep(time);
            manager.release(initialPermit);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
