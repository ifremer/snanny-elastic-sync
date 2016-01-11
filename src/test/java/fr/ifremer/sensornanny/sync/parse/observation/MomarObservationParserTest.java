package fr.ifremer.sensornanny.sync.parse.observation;

import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.parse.observations.impl.MomarObservationParser;

@Category(UnitTest.class)
public class MomarObservationParserTest extends UnitTest {

    private MomarObservationParser parser = new MomarObservationParser();

    @Test
    public void testParseMomarFile() {
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream("momar/obs/optode2011.csv");
        parser.read("optode2011.csv", resource, new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                org.junit.Assert.assertNotNull(t);
            }
        });
    }
}
