package fr.ifremer.sensornanny.sync.parse.observations.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.parse.observations.IObservationParser;
import fr.ifremer.sensornanny.sync.util.DateUtils;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

/**
 * Concrete parser for momar observation file
 * 
 * @author athorel
 *
 */
public class NetCdfObservationParser implements IObservationParser<TimePosition> {

    private static final String DEPTH_SECTION = "depth";

    private static final String TIME_SECTION = "time";

    private static final String LAT_SECTION = "lat";

    private static final String LONG_SECTION = "long";

    private static final String ACCEPTED_FORMAT = "application/netcdf";

    @Override
    public void read(String fileName, InputStream stream, Consumer<TimePosition> consumer) {
        NetcdfFile cdfFile = null;
        try {
            cdfFile = NetcdfFile.openInMemory(fileName, ucar.nc2.util.IO.readContentsToByteArray(stream));
            Array longitudeSection = cdfFile.readSection(LONG_SECTION);
            Array latitudeSection = cdfFile.readSection(LAT_SECTION);
            Array timeSection = cdfFile.readSection(TIME_SECTION);
            Array depthSection = cdfFile.readSection(DEPTH_SECTION);

            long size = longitudeSection.getSize();
            for (int i = 0; i < size; i++) {
                TimePosition timePosition = new TimePosition();
                timePosition.setRecordNumber(Long.valueOf(i));
                timePosition.setLongitude(longitudeSection.getFloat(i));
                timePosition.setLatitude(latitudeSection.getFloat(i));
                timePosition.setDepth(depthSection.getFloat(i));
                timePosition.setDate(DateUtils.randomDateTransform(timeSection.getDouble(i)));
                consumer.accept(timePosition);
            }

        } catch (Exception e) {
            // Nothing to do
        } finally {
            closeQuietly(cdfFile);
        }
    }

    @Override
    public boolean accept(String role) {
        return ACCEPTED_FORMAT.equalsIgnoreCase(role);
    }

    private void closeQuietly(NetcdfFile file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                // Nothing todo
            }
        }
    }
}
