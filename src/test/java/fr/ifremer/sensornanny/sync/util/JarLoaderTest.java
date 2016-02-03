package fr.ifremer.sensornanny.sync.util;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.observation.parser.IObservationParser;
import fr.ifremer.sensornanny.observation.parser.ObservationData;
import fr.ifremer.sensornanny.observation.parser.TimePosition;
import fr.ifremer.sensornanny.sync.base.UnitTest;

public class JarLoaderTest extends UnitTest {

    @Test
    public void testLoadExternalJar() {
        URL resource = getClass().getClassLoader().getResource("concreteParsers");
        URL[] discoverJars = JarLoader.discoverJars(resource.getPath());

        List<IObservationParser> loadJars = JarLoader.scanForInterfaces(discoverJars, IObservationParser.class,
                "fr.ifremer");
        System.out.println(loadJars);
        ObservationData data = ObservationData.of("201304010045-shipnav-TL_CINNA.nav", "application/netcdf");
        InputStream load = load("netcdf/201304010045-shipnav-TL_CINNA.nav");
        boolean loaded = false;
        for (IObservationParser parser : loadJars) {
            if (parser.accept(data)) {
                loaded = true;
                parser.read(data, load, new Consumer<TimePosition>() {

                    @Override
                    public void accept(TimePosition t) {
                        Assert.assertNotNull(t);
                    }
                });
            }
        }
        Assert.assertTrue(loaded);
    }

}
