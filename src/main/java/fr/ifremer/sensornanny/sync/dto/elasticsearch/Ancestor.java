package fr.ifremer.sensornanny.sync.dto.elasticsearch;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * System representation
 * 
 * @author athorel
 *
 */
public class Ancestor {

    /** Unique identifier of the system observations */
    @SerializedName("snanny-ancestor-deploymentid")
    private String deploymentId;
    /** Unique identifier of the system */
    @SerializedName("snanny-ancestor-uuid")
    private String uuid;
    /** Name of the system */
    @SerializedName("snanny-ancestor-name")
    private String name;
    /** Description of the system */
    @SerializedName("snanny-ancestor-description")
    private String description;
    /** Terms */
    @SerializedName("snanny-ancestor-terms")
    private List<String> terms;
    /** Keywords */
    @SerializedName("snanny-ancestor-keywords")
    private List<String> keywords;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

}
