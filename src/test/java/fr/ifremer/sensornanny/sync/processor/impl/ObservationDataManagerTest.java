package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.InputStream;
import java.util.function.Consumer;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.MockTest;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.model.OMResult;
import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.parse.observations.impl.MomarObservationParser;
import fr.ifremer.sensornanny.sync.parse.observations.impl.NetCdfObservationParser;

public class ObservationDataManagerTest extends MockTest {

    @TestSubject
    ObservationDataManager manager = new ObservationDataManager();

    @Mock
    MomarObservationParser momarParser;

    @Mock
    NetCdfObservationParser netCdfParser;

    @Mock
    IOwncloudDao owncloudDao;

    @Test
    public void testManagerIT() throws InterruptedException {
        Capture<Integer> capture = EasyMock.newCapture();
        OMResult omResult = new OMResult();
        omResult.setRole("application/netcdf");
        String fileName = "file1.nav";
        omResult.setUrl(fileName);

        FileSizeInfo value = new FileSizeInfo();
        int maxMemory = Config.maxMemory();
        // Acquire hal permits
        int initialPermit = maxMemory / 2;
        int fileInMO = initialPermit - 3;
        // Set Size is next all permits
        long i = fileInMO * ObservationDataManager.ONE_MEGA_OCTECT_IN_OCTET;
        value.setFileSize(Long.valueOf(i));
        expect(momarParser.accept(omResult.getRole())).andReturn(false);
        expect(netCdfParser.accept(omResult.getRole())).andReturn(true);
        long idOM = 1L;
        expect(owncloudDao.getFileSize(idOM, fileName)).andReturn(value);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("momar/obs/optode2011.csv");
        expect(owncloudDao.getResultData(idOM, fileName)).andReturn(resourceAsStream);

        Consumer<TimePosition> consumer = new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                capture.setValue(manager.drainPermits());
            }
        };

        netCdfParser.read("file1.nav", resourceAsStream, consumer);
        expectLastCall().andStubDelegateTo(new NetCdfObservationParser() {

            @Override
            public void read(String fileName, InputStream stream, Consumer<TimePosition> consumer) {
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
                manager.readData(idOM, omResult, consumer);
            }
        });
        thread.start();
        Thread.sleep(800);
        Assert.assertEquals(Config.maxMemory() - initialPermit - fileInMO, capture.getValue().intValue());
    }

    @Test
    public void testManagerWaitIT() throws InterruptedException {

        Capture<Integer> capture = EasyMock.newCapture();
        OMResult omResult = new OMResult();
        omResult.setRole("application/netcdf");
        String fileName = "file1.nav";
        omResult.setUrl(fileName);

        FileSizeInfo value = new FileSizeInfo();
        int maxMemory = Config.maxMemory();
        // Acquire hal permits
        final int permits = maxMemory / 2;

        int fileInMo = permits + 4;
        // Set Size is next all permits
        final long i = fileInMo * ObservationDataManager.ONE_MEGA_OCTECT_IN_OCTET;
        value.setFileSize(Long.valueOf(i));
        expect(momarParser.accept(omResult.getRole())).andReturn(false);
        expect(netCdfParser.accept(omResult.getRole())).andReturn(true);
        long idOM = 1L;
        expect(owncloudDao.getFileSize(idOM, fileName)).andReturn(value);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("momar/obs/optode2011.csv");
        expect(owncloudDao.getResultData(idOM, fileName)).andReturn(resourceAsStream);

        Consumer<TimePosition> consumer = new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                capture.setValue(manager.drainPermits());
            }
        };

        netCdfParser.read("file1.nav", resourceAsStream, consumer);
        expectLastCall().andStubDelegateTo(new NetCdfObservationParser() {

            @Override
            public void read(String fileName, InputStream stream, Consumer<TimePosition> consumer) {
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
                manager.readData(idOM, omResult, consumer);
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
