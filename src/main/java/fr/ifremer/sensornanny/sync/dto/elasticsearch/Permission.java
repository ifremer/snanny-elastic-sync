package fr.ifremer.sensornanny.sync.dto.elasticsearch;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Permission {

    /**
     * Permission status 0->private, 1->limited, 2->public
     */
    @SerializedName("snanny-access-type")
    private int status;
    /**
     * List of authorizedPersonne when status is limited (1)
     */
    @SerializedName("snanny-access-auth")
    private List<String> authorized;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getAuthorized() {
        return authorized;
    }

    public void setAuthorized(List<String> authorized) {
        this.authorized = authorized;
    }

}
