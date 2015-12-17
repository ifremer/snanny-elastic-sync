package fr.ifremer.sensornanny.sync.dto.owncloud;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Model representation of ancestors
 * 
 * @author athorel
 *
 */
public class SensorMLAncestors {

    @SerializedName("ancestors")
    List<String> ancestors;

    public List<String> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<String> ancestors) {
        this.ancestors = ancestors;
    }

}
