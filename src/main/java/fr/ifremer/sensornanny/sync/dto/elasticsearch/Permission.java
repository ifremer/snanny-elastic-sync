package fr.ifremer.sensornanny.sync.dto.elasticsearch;

import java.util.List;

public class Permission {

    /**
     * Permission status 0->private, 1->limited, 2->public
     */
    private int status;
    /**
     * List of authorizedPersonne when status is limited (1)
     */
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
