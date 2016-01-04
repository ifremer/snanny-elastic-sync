package fr.ifremer.sensornanny.sync.dto.owncloud;

public class IndexStatusResponse {

    private String status;
    private String uuid;
    private String time;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "IndexStatusResponse [status=" + status + ", uuid=" + uuid + ", time=" + time + "]";
    }

}
