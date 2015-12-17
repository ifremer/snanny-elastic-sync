package fr.ifremer.sensornanny.sync.dto.model;

import java.util.List;

/**
 * Simple representation of an axis
 * 
 * @author athorel
 *
 */
public class Axis {

    public static Axis from(List<Double> list) {
        Axis axis = new Axis();
        switch (list.size()) {
            case 3:
                axis.setDep(list.get(2));
            case 2:
                axis.setLon(list.get(1));
            case 1:
                axis.setLat(list.get(0));

        }
        return axis;
    }

    /** Latitude */
    private Number lat;
    /** Logitude */
    private Number lon;
    /** Depth (Z-index) */
    private Number dep;

    public Number getLat() {
        return lat;
    }

    public void setLat(Number lat) {
        this.lat = lat;
    }

    public Number getLon() {
        return lon;
    }

    public void setLon(Number lon) {
        this.lon = lon;
    }

    public Number getDep() {
        return dep;
    }

    public void setDep(Number dep) {
        this.dep = dep;
    }

}
