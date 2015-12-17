package fr.ifremer.sensornanny.sync.dto.owncloud;

public class Content {

    private String path;
    private String user;
    private String[] parent;
    private String content;

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    public String[] getParent() {
        return parent;
    }

    public String getContent() {
        return content;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setParent(String[] parent) {
        this.parent = parent;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
