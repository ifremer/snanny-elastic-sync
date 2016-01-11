package fr.ifremer.sensornanny.sync.dto.owncloud;

import com.google.gson.annotations.SerializedName;

/**
 * Indexation status of on OM file
 * 
 * @author athorel
 */
public class IndexStatus {

    private static final String SUCCES_MESSAGE = "succes";

    @SerializedName("fileId")
    private Long fileId;
    @SerializedName("uuid")
    private String uuid;
    @SerializedName("time")
    private Long time;
    @SerializedName("status")
    private boolean status = true;
    @SerializedName("message")
    private String message = SUCCES_MESSAGE;
    @SerializedName("indexedObservations")
    private int indexedObservations;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getIndexedObservations() {
        return indexedObservations;
    }

    public void setIndexedObservations(int indexedObservations) {
        this.indexedObservations = indexedObservations;
    }

    public void increaseIndexed() {
        indexedObservations++;
    }

    @Override
    public String toString() {
        return "IndexStatus [fileId=" + fileId + ", uuid=" + uuid + ", status=" + status + ", message=" + message
                + ", indexedObservations=" + indexedObservations + "]";
    }

}
