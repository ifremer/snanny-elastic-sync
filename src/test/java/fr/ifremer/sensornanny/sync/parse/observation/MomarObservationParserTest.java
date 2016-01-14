package fr.ifremer.sensornanny.sync.parse.observation;

import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.parse.observations.impl.MomarObservationParser;

public class MomarObservationParserTest extends UnitTest {

    private MomarObservationParser parser = new MomarObservationParser();

    @Test
    public void testParseMomarFile() {
        InputStream resource = load("momar/obs/optode2011.csv");
        parser.read("optode2011.csv", resource, new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                Assert.assertNotNull(t);
            }
        });
    }
}
