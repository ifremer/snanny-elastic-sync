package fr.ifremer.sensornanny.sync.dto.owncloud;

import com.google.gson.annotations.SerializedName;

public class OwncloudSyncModel {

    private String uuid;
    @SerializedName("file_id")
    private Long fileId;
    private String name;
    private String description;
    private int status;
    @SerializedName("system_uuid")
    private String systemUuid;
    @SerializedName("result_file")
    private String resultFile;

    private int shared;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStatus() {
        return status == 1;
    }

    public String getSystemUuid() {
        return systemUuid;
    }

    public void setSystemUuid(String systemUuid) {
        this.systemUuid = systemUuid;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public boolean isShared() {
        return shared == 1;
    }

    @Override
    public String toString() {
        return "OwncloudSyncModel [uuid=" + uuid + ", fileId=" + fileId + ", name=" + name + ", description="
                + description + ", status=" + isStatus() + ", systemUuid=" + systemUuid + ", resultFile=" + resultFile
                + ", shared=" + isShared() + "]";
    }

}
