package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.InputStream;
import java.util.function.Consumer;

import fr.ifremer.sensornanny.observation.parser.IObservationParser;
import fr.ifremer.sensornanny.observation.parser.ObservationData;
import fr.ifremer.sensornanny.observation.parser.TimePosition;

public class FakeParser implements IObservationParser {

    @Override
    public boolean accept(ObservationData data) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void read(ObservationData data, InputStream stream, Consumer<TimePosition> consumer) {
        // TODO Auto-generated method stub

    }
}
