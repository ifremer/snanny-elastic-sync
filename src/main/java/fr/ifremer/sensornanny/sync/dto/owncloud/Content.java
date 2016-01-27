package fr.ifremer.sensornanny.sync.dto.owncloud;

import java.util.List;

public class Content {

    private String user;
    private String content;
    private List<Share> shares;

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Share> getShares() {
        return shares;
    }

    public void setShares(List<Share> shares) {
        this.shares = shares;
    }

}
