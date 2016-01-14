package fr.ifremer.sensornanny.sync.parse.observation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.parse.observations.impl.NetCdfObservationParser;

public class NetCDFObservationParserTest extends UnitTest {

    private NetCdfObservationParser parser = new NetCdfObservationParser();

    @Test
    public void testOpenFile() throws MalformedURLException, IOException {
        InputStream stream = load("netcdf/201304010045-shipnav-TL_CINNA.nav");
        String fileName = "201304010045-shipnav-TL_CINNA.nav";
        parser.read(fileName, stream, new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                Assert.assertNotNull(t);

            }
        });
    }

    @Test
    public void testOpenFileNotNav() throws MalformedURLException, IOException {
        InputStream stream = load("sensorML/atalante_sensorML.xml");
        String fileName = "atalante_sensorML.xml";
        parser.read(fileName, stream, new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                Assert.fail("Shouldn't passed here");
            }
        });
    }
}
