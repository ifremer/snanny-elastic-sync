package fr.ifremer.sensornanny.sync.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import fr.ifremer.sensornanny.sync.dto.elasticsearch.Permission;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.PermissionStatus;
import fr.ifremer.sensornanny.sync.dto.owncloud.Share;

/**
 * Class that allow to transform list of owncloud shares to Observations json permissions
 * 
 * @author athorel
 *
 */
public class PermissionsConverter {

    /**
     * Extract permissions from shares
     * 
     * @param shares list of shares from owncloud
     * @return permission object containing permission level, and optionally the list of recipients
     */
    public Permission extractPermissions(List<Share> shares) {
        Permission permission = new Permission();
        if (CollectionUtils.isNotEmpty(shares)) {
            ArrayList<String> authorized = new ArrayList<>();

            for (Share share : shares) {
                if (StringUtils.isBlank(share.getShareWith())) {
                    permission.setStatus(PermissionStatus.PUBLIC.getStatus());
                    authorized.clear();
                    break;
                } else {
                    permission.setStatus(PermissionStatus.SHARED.getStatus());
                    authorized.add(share.getShareWith());
                }
            }
            permission.setAuthorized(authorized);
        } else {
            permission.setStatus(PermissionStatus.PRIVATE.getStatus());
        }
        return permission;
    }
}
