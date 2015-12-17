package fr.ifremer.sensornanny.sync.builder;

import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;

public class ActivityBuilder {

    private Long fileId;
    private String filePath;
    private String userName;
    private String type;

    public ActivityBuilder() {

    }

    public ActivityBuilder id(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public ActivityBuilder path(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public ActivityBuilder withUser(String userName) {
        this.userName = userName;
        return this;
    }

    public ActivityBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public Activity build() {
        Activity activity = new Activity();
        activity.setFileId(fileId);
        activity.setFilePath(filePath);
        activity.setType(type);
        activity.setUserName(userName);
        return activity;
    }

    public static Activity simpleActivity(Long fileId, String filePath, String type, String userName) {
        return new ActivityBuilder().id(fileId).path(filePath).withType(type).withUser(userName).build();
    }

}
