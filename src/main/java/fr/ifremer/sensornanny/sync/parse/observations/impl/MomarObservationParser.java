package fr.ifremer.sensornanny.sync.parse.observations.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.parse.observations.IObservationParser;
import fr.ifremer.sensornanny.sync.util.DateUtils;

/**
 * Concrete parser for momar observation file
 * 
 * @author athorel
 *
 */
public class MomarObservationParser implements IObservationParser<TimePosition> {

    private static final String ACCEPTED_FORMAT = "txt/csv";
    private static final int LON_INDEX = 3;
    private static final int LAT_INDEX = 2;
    private static final int DATE_INDEX = 1;

    @Override
    public void read(String fileName, InputStream stream, Consumer<TimePosition> consumer) {
        CSVParser parser = null;
        try {
            parser = new CSVParser(new InputStreamReader(stream), CSVFormat.DEFAULT);
            Iterator<CSVRecord> iterator = parser.iterator();

            if (iterator.hasNext()) {
                // skip header
                iterator.next();
            }

            // ReadData
            iterator.forEachRemaining(new Consumer<CSVRecord>() {

                @Override
                public void accept(CSVRecord t) {

                    TimePosition timePosition = new TimePosition();
                    timePosition.setDate(DateUtils.parse(t.get(DATE_INDEX)));
                    timePosition.setLatitude(safeFloatValue(t.get(LAT_INDEX)));
                    timePosition.setLongitude(safeFloatValue(t.get(LON_INDEX)));
                    timePosition.setRecordNumber(t.getRecordNumber());

                    consumer.accept(timePosition);

                }

            });

        } catch (Exception e) {
            // Nothing todo
        } finally {
            IOUtils.closeQuietly(parser);
        }
    }

    private Float safeFloatValue(String value) {
        if (StringUtils.isNotEmpty(value)) {
            try {
                return Float.valueOf(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean accept(String role) {
        return ACCEPTED_FORMAT.equalsIgnoreCase(role);
    }

}
