package fr.ifremer.sensornanny.sync.dao.impl;

import com.google.gson.JsonObject;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.ISystemDao;
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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;

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
            .setBulkActions(1000)
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
        int items = search.getHits().hits().length;
        while (items > 0) {
            // Create a bulk item of deletion
            BulkRequestBuilder bulk = client.prepareBulk();
            search.getHits().forEach(new Consumer<SearchHit>() {
                @Override
                public void accept(SearchHit t) {
                    bulk.add(client.prepareDelete(Config.systemsIndex(), SNANNY_SYSTEMS, t.getId()).request());
                }
            });
            // Execute the bulk
            bulk.execute();
            // Get the next page
            search = client.prepareSearch(Config.systemsIndex()).setQuery(QueryBuilders.wildcardQuery(
                    SNANNY_UUID, uuid + WILDCARDS)).setScroll(scroll).setSize(NUMBER_OF_ITEMS_PER_DELETION).get();
            items = search.getHits().hits().length;
        }
    }


    @Override
    public boolean write(String uuid, boolean hasData, OwncloudSyncModel om) {
        try {
            JsonObject item = new JsonObject();
            item.addProperty(SNANNY_UUID, uuid);
            item.addProperty(SNANNY_SYSTEM_NAME, om.getName());
            item.addProperty(SNANNY_SYSTEM_DESCRIPTION, om.getDescription());
            item.addProperty(SNANNY_SYSTEM_UUID, om.getSystemUuid());

            item.addProperty(SNANNY_SYSTEM_FILEID, om.getFileId());
            item.addProperty(SNANNY_SYSTEM_RESULTFILE, om.getResultFile());
            item.addProperty(SNANNY_SYSTEM_HASDATA, hasData);

            UpdateRequest updateRequest = new UpdateRequest(Config.systemsIndex(), SNANNY_SYSTEMS, uuid);
            updateRequest.doc(item.toString()).upsert(item.toString());

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

    private Client getClient() {
        return NodeManager.getInstance().getClient();
    }

}
