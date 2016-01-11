package fr.ifremer.sensornanny.sync.dto.owncloud;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author athorel
 *
 */
public class Share {

    /**
     * Share type
     */
    @SerializedName("share_type")
    private int shareType;

    /**
     * Share with
     */
    @SerializedName("share_with")
    private String shareWith;

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public String getShareWith() {
        return shareWith;
    }

    public void setShareWith(String shareWith) {
        this.shareWith = shareWith;
    }

    @Override
    public String toString() {
        return "[shareType=" + shareType + ", shareWith=" + shareWith + "]";
    }

}
