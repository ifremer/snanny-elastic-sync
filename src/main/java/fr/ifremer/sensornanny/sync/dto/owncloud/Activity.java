package fr.ifremer.sensornanny.sync.dto.owncloud;

import com.google.gson.annotations.SerializedName;

/**
 * Representation of an activity event on owncloud
 * 
 * @author athorel
 *
 */
public class Activity {

    @SerializedName("object_id")
    private Long fileId;
    @SerializedName("file")
    private String filePath;
    @SerializedName("user")
    private String userName;
    @SerializedName("type")
    private String type;

    public Long getFileId() {
        return fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUserName() {
        return userName;
    }

    public String getType() {
        return type;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Activity [fileId=" + fileId + ", filePath=" + filePath + ", userName=" + userName + ", type=" + type
                + "]";
    }

}
