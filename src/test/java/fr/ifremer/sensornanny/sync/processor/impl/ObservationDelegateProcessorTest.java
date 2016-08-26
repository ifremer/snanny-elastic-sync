package fr.ifremer.sensornanny.sync.processor.impl;

import fr.ifremer.sensornanny.sync.base.MockTest;
import fr.ifremer.sensornanny.sync.cache.impl.SensorMLCacheManager;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Ancestor;
import fr.ifremer.sensornanny.sync.dto.model.Axis;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by athorel on 26/08/2016.
 */
public class ObservationDelegateProcessorTest extends MockTest {

    @TestSubject
    private ObservationDelegateProcessorImpl processor = new ObservationDelegateProcessorImpl();

    @Mock
    private SensorMLCacheManager cacheSystem;

    @Test
    public void testGetFirstAncestorWithCoordinates() {

        Ancestor cinna = ancestor("CINNA");
        Ancestor tsg = ancestor("TSG");
        Ancestor borel = ancestor("Borel");


        List<Ancestor> ancestors = Arrays.asList(borel, tsg, cinna);

        //Prepare mock
        expect(cacheSystem.getData(cinna.getUuid(), null, null)).andReturn(new SensorML());
        expect(cacheSystem.getData(tsg.getUuid(), null, null)).andReturn(new SensorML());

        SensorML value = new SensorML();
        Axis coordinate = new Axis();
        coordinate.setLat(10);
        coordinate.setLon(25);
        coordinate.setDep(0);
        value.setCoordinate(coordinate);
        expect(cacheSystem.getData(borel.getUuid(), null, null)).andReturn(value);

        replayAll();
        //Execute

        Axis firstValidAxisInSML = processor.getFirstValidAxisInSML(ancestors, null, null);
        assertEquals(coordinate, firstValidAxisInSML);
    }

    @Test
    public void testGetFirstAncestorWithCoordinatesWithNoItem() {

        Ancestor cinna = ancestor("CINNA");
        List<Ancestor> ancestors = Arrays.asList(cinna);

        //Prepare mock
        expect(cacheSystem.getData(cinna.getUuid(), null, null)).andReturn(new SensorML());

        replayAll();
        //Execute

        Axis firstValidAxisInSML = processor.getFirstValidAxisInSML(ancestors, null, null);
        assertNull(firstValidAxisInSML);
    }

    private Ancestor ancestor(String uuid) {
        Ancestor ancestor = new Ancestor();
        ancestor.setUuid(uuid);
        return ancestor;
    }
}
