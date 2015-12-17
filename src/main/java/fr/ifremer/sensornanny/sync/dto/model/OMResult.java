package fr.ifremer.sensornanny.sync.dto.model;

/**
 * Class representation of a om:result
 * 
 * @author athorel
 *
 */
public class OMResult {
    /** URL of the the result file */
    private String url;
    /** Type of file */
    private String role;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
