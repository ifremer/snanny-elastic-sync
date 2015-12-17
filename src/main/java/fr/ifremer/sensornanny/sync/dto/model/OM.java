package fr.ifremer.sensornanny.sync.dto.model;

import java.util.Date;

/**
 * Simple representation of an OM observation
 * 
 * @author athorel
 *
 */
public class OM {
    /** UUID */
    private String identifier;
    /** Name */
    private String name;
    /** Description */
    private String description;

    /** Lower corner of the observation */
    private Axis lowerCorner;
    /** upper corner of the observation */
    private Axis upperCorner;

    /** Start time for the observation */
    private Date beginPosition;
    /** End time for the observation */
    private Date endPosition;

    /** Time of the file disk */
    private Date updateDate;

    /** Procedure used for the observation */
    private String procedure;
    /** Procedure result */
    private OMResult result;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Axis getLowerCorner() {
        return lowerCorner;
    }

    public void setLowerCorner(Axis lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public Axis getUpperCorner() {
        return upperCorner;
    }

    public void setUpperCorner(Axis upperCorner) {
        this.upperCorner = upperCorner;
    }

    public Date getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(Date beginPosition) {
        this.beginPosition = beginPosition;
    }

    public Date getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Date endPosition) {
        this.endPosition = endPosition;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public OMResult getResult() {
        return result;
    }

    public void setResult(OMResult result) {
        this.result = result;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

}
