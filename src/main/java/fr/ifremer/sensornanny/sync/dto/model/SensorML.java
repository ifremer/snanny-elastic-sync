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
}
