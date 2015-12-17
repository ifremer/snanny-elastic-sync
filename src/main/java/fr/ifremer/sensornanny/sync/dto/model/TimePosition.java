package fr.ifremer.sensornanny.sync.dto.model;

import java.util.Date;

public class TimePosition {

    private Long recordNumber;
    private Date date;
    private Float latitude;
    private Float longitude;
    private Float depth;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Long getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(Long recordNumber) {
        this.recordNumber = recordNumber;
    }

    public Float getDepth() {
        return depth;
    }

    public void setDepth(Float depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        return "TimePosition [recordNumber=" + recordNumber + ", date=" + date + ", latitude=" + latitude
                + ", longitude=" + longitude + ", depth=" + depth + "]";
    }

}
