package fr.ifremer.sensornanny.sync.dto.elasticsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representation of an observation
 * 
 * @author athorel
 *
 */
public class ObservationJson {

    /** Unique identifier of the ancestor observations */
    private String deploymentId;

    /** System uuid **/
    private String uuid;

    /** Coordinates of the observation */
    private String coordinates;

    /** Name of the observation */
    private String name;

    /** author name of the observation file */
    private String author;

    /** Description of the observation */
    private String description;

    /** Family */
    private String family;

    /** List of ancestors (systems which allow this observation) */
    private List<Ancestor> ancestors = new ArrayList<>();

    /** Time of the observations */
    private Date resultTimestamp;

    /** Time of the uploaded document */
    private Date updateTimestamp;

    /** Result name of the observation allow-accessing the file */
    private String result;

    /** Depth of the observation */
    private Number depth;

    private Permission permission;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setAncestors(List<Ancestor> ancestors) {
        this.ancestors = ancestors;
    }

    public void setResultTimestamp(Date resultTimestamp) {
        this.resultTimestamp = resultTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getFamily() {
        return family;
    }

    public List<Ancestor> getAncestors() {
        return ancestors;
    }

    public Date getResultTimestamp() {
        return resultTimestamp;
    }

    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public String getResult() {
        return result;
    }

    public Number getDepth() {
        return depth;
    }

    public void setDepth(Number depth) {
        this.depth = depth;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return "ObservationJson{" +
                "deploymentId='" + deploymentId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
