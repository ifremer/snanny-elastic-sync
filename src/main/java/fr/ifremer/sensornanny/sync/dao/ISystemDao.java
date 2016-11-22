package fr.ifremer.sensornanny.sync.dao;

import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

/**
 * Interface that allow to access to elastic
 * 
 * @author athorel
 *
 */
public interface ISystemDao {

    /**
     * Delete all the system items represented by O&M uuid,
     * Execute the search on snanny-uuid = uuid* and return list of observation with id uuid-0001, uuid-0002, ..., etc
     * 
     * @param uuid unique identifier of the observation O&M file
     */
    void delete(String uuid);

    /**
     * Insert or update an observation
     * 
     * @param om Representation object of an system
     * @return <code>true</code> when the observations have been correctly inserted or updated, otherwise
     *         <code>false</code>
     */
    boolean write(String uuid, boolean hasData, OwncloudSyncModel om);
}
