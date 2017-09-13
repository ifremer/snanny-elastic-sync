package fr.ifremer.sensornanny.sync.writer;

import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;

/**
 * Class allow to write to elasticSearch an element
 * 
 * @author athorel
 *
 */
public interface IElasticWriter {

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
     * @param uuid unique identifier of an observation
     * @param observation Representation object of an observation
     * @return <code>true</code> when the observations have been correctly inserted or updated, otherwise
     *         <code>false</code>
     */
    boolean write(String uuid, ObservationJson observation);

    /**
     * Insert or update a system
     *
     * @param uuid unique identifier of a system
     * @param hasData has the system linked data ?
     * @param sml Representation object for a system
     * @return <code>true</code> when the system have been correctly inserted or updated, otherwise
     *         <code>false</code>
     */
    boolean write(String uuid, SensorML sml, boolean hasData);

    /**
     * Flush writers to ensure all data are written by the bulk processors
     */
    void flush();
}
