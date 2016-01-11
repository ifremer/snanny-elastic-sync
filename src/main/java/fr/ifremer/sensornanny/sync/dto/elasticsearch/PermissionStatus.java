package fr.ifremer.sensornanny.sync.dto.elasticsearch;

/**
 * List of shares status
 * 
 * @author athorel
 *
 */
public enum PermissionStatus {

    /** Observation not shared */
    PRIVATE(0),

    /** Observation shared with specific people */
    SHARED(1),

    /** Observation shared with public */
    PUBLIC(2);

    private int status;

    private PermissionStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
