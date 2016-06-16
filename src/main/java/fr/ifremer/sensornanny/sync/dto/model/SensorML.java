package fr.ifremer.sensornanny.sync.dto.model;

import java.util.List;

public class SensorML {

    /** Identifier */
    private String uuid;
    /** Name */
    private String name;
    /** Descriptions */
    private String description;
    /** Terms */
    private List<String> terms;
    /** Keywords */
    private List<String> keywords;
    /** Components */
    private List<Comp> components;
    /** axis of the system */
    private Axis coordinate;
    /** start time of the system **/
    private Long startTime;
    /** end time of the system **/
    private Long endTime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public Axis getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Axis coordinate) {
        this.coordinate = coordinate;
    }

    public List<Comp> getComponents() {
        return components;
    }

    public void setComponents(List<Comp> components) {
        this.components = components;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "SensorML{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", terms=" + terms +
                ", keywords=" + keywords +
                ", components=" + components +
                ", coordinate=" + coordinate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
