package fr.ifremer.sensornanny.sync.dto.elasticsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Representation of an observation
 * 
 * @author athorel
 *
 */
public class ObservationJson {

    /** Unique identifier of the observations */
    @SerializedName("snanny-uuid")
    private String uuid;
    /** Coordinates of the observation */
    @SerializedName("snanny-coordinates")
    private Coordinates coordinates;
    /** Name of the observation */
    @SerializedName("snanny-name")
    private String name;
    /** author name of the observation file */
    @SerializedName("snanny-author")
    private String author;
    /** Description of the observation */
    @SerializedName("snanny-description")
    private String description;
    /** Family */
    @SerializedName("snanny-family")
    private String family;
    /** List of ancestors (systems which allow this observation) */
    @SerializedName("snanny-ancestors")
    private List<Ancestor> ancestors = new ArrayList<>();
    /** Time of the observations */
    @SerializedName("snanny-resulttimestamp")
    private Date resultTimestamp;
    /** Time of the uploaded document */
    @SerializedName("snanny-updatetimestamp")
    private Date updateTimestamp;
    /** Result name of the observation allow-accessing the file */
    @SerializedName("snanny-resultfile")
    private String result;
    /** Depth of the observation */
    @SerializedName("snanny-depth")
    private Number depth;

    @SerializedName("snanny-access")
    private Permission permission;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCoordinates(Coordinates coordinates) {
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

    public Coordinates getCoordinates() {
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
        return "ObservationJson [uuid=" + uuid + ", name=" + name + ", description=" + description + "]";
    }

}
