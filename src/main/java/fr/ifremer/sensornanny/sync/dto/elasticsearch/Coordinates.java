package fr.ifremer.sensornanny.sync.dto.elasticsearch;

/**
 * Coordinates representation
 * 
 * @author athorel
 *
 */
public class Coordinates {
    /** Latitude of the point */
    private Number lat;
    /** Longitude of the point */
    private Number lon;

    public void setLat(Number lat) {
        this.lat = lat;
    }

    public void setLon(Number lon) {
        this.lon = lon;
    }

    public Number getLat() {
        return lat;
    }

    public Number getLon() {
        return lon;
    }

}
