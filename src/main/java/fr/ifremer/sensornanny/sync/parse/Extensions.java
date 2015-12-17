package fr.ifremer.sensornanny.sync.parse;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author athorel
 *
 */
public enum Extensions {

    /** SensorML files */
    SENSOR_ML("sensorML.xml"),
    /** XML files */
    XML(".xml"),

    /** Netcfd files */
    NETCFD(".nav, .htc"),

    /** Csv files */
    CSV(".csv");

    /** Declaration of the extensions */
    List<String> exts;

    /**
     * Constructor using extensions
     * 
     * @param exts list of extensions
     */
    private Extensions(String... exts) {
        this.exts = Arrays.asList(exts);
    }

    public boolean accept(String name) {
        if (StringUtils.isNotBlank(name)) {
            for (String ext : exts) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }
}
