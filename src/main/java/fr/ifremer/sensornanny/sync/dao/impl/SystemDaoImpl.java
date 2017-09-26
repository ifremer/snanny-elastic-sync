package fr.ifremer.sensornanny.sync.dao.impl;

import com.google.gson.JsonObject;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.ElasticSearchBulkProcessor;
import fr.ifremer.sensornanny.sync.dao.ISystemDao;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
import fr.ifremer.sensornanny.sync.manager.NodeManager;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.ifremer.sensornanny.sync.constant.ObservationsFields.*;

/**
 * Implementation of elastic Dao
 * 
 * @author athorel
 *
 */
public class SystemDaoImpl implements ISystemDao {

    private static final Logger LOGGER = Logger.getLogger(SystemDaoImpl.class.getName());
    private static final int NUMBER_OF_ITEMS_PER_DELETION = 10000;
    private static final String WILDCARDS = "*";

    private BulkProcessor bulkProcessor = BulkProcessor.builder(
            getClient(),
            new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                    if (response.hasFailures()) {
                        LOGGER.warning(response.buildFailureMessage());
                    }
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    LOGGER.warning(failure.getMessage());
                }

            })
            .setBulkActions(100)
            .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
            .setFlushInterval(TimeValue.timeValueSeconds(5))
            .setConcurrentRequests(1)
            .build();

    @Override
    public void delete(String uuid) {
        Client client = getClient();

        Scroll scroll = new Scroll(new TimeValue(2000));

        // Create a scroll request
        SearchResponse search = client.prepareSearch(Config.systemsIndex()).setQuery(QueryBuilders.wildcardQuery(
                SNANNY_UUID, uuid + WILDCARDS)).setScroll(scroll).setSize(NUMBER_OF_ITEMS_PER_DELETION).get();

        // While there are items to delete
        int items = search.getHits().getHits().length;
        while (items > 0) {
            // Create a bulk item of deletion
            BulkRequestBuilder bulk = client.prepareBulk();
            search.getHits().forEach((SearchHit t) ->
                    bulk.add(client.prepareDelete(Config.systemsIndex(), SNANNY_SYSTEMS, t.getId()).request())
            );
            // Execute the bulk
            bulk.execute();
            // Get the next page
            search = client.prepareSearchScroll(search.getScrollId()).setScroll(scroll).execute().actionGet();
            items = search.getHits().getHits().length;
        }
    }


    @Override
    public boolean write(String uuid, SensorML system, boolean hasData) {
        try {
            JsonObject item = new JsonObject();
            item.addProperty(SNANNY_UUID, uuid);
            item.addProperty(SNANNY_SYSTEM_NAME, system.getName());
            item.addProperty(SNANNY_SYSTEM_DESCRIPTION, system.getDescription());
            item.addProperty(SNANNY_SYSTEM_UUID, system.getUuid());
            item.addProperty(SNANNY_SYSTEM_HASDATA, hasData);

            UpdateRequest updateRequest = new UpdateRequest(Config.systemsIndex(), SNANNY_SYSTEMS, uuid);
            updateRequest.doc(item.toString(), XContentType.JSON).upsert(item.toString(), XContentType.JSON);

            bulkProcessor.add(updateRequest);
            return true;
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.SEVERE, "ElasticSearch is not listening", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "entry " + uuid + " won't be write in elasticsearch", e);
            return false;
        }
    }

    @Override
    public void flush() {
        try {
            bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
            LOGGER.log(Level.INFO, "system data flushed");
        } catch(InterruptedException ie){
            LOGGER.log(Level.SEVERE, "couldn't close system dao properly",ie);
        }
    }

    private Client getClient() {
        return NodeManager.getInstance().getClient();
    }

}
