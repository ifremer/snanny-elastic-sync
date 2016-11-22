package fr.ifremer.sensornanny.sync.dao;

import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

/**
 * Interface that allow to access to elastic
 * 
 * @author athorel
 *
 */
public interface IObservationDao {

    /**
     * Delete all the observations items represented by O&M uuid,
     * Execute the search on snanny-uuid = uuid* and return list of observation with id uuid-0001, uuid-0002, ..., etc
     * 
     * @param uuid unique identifier of the observation O&M file
     */
    void delete(String uuid);

    /**
     * Insert or update an observation
     * 
     * @param observation Representation object of an observation
     * @return <code>true</code> when the observations have been correctly inserted or updated, otherwise
     *         <code>false</code>
     */
    boolean write(String uuid, ObservationJson observation);
}
