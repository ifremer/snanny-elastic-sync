package fr.ifremer.sensornanny.sync.dao.impl;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IElasticDao;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.manager.NodeManager;

/**
 * Implementation of elastic Dao
 * 
 * @author athorel
 *
 */
public class ElasticDaoImpl implements IElasticDao {

    private static final Logger LOGGER = Logger.getLogger(ElasticDaoImpl.class.getName());
    private static final int NUMBER_OF_ITEMS_PER_DELETION = 10000;
    private static final String WILDCARDS = "*";
    private static final String SNANNY_UUID = "snanny-uuid";
    private static final String DOC_SUFFIX = "}";
    private static final String DOC_PREFIX = "{\"doc\":";
    private static final String OBJECT_NAME = "snanny-observation";

    @Override
    public void delete(String uuid) {
        Client client = getClient();

        Scroll scroll = new Scroll(new TimeValue(2000));

        // Create a scroll request
        SearchResponse search = client.prepareSearch(Config.observationsIndex()).setQuery(QueryBuilders.wildcardQuery(
                SNANNY_UUID, uuid + WILDCARDS)).setScroll(scroll).setSize(NUMBER_OF_ITEMS_PER_DELETION).get();

        // While there are items to delete
        int items = search.getHits().hits().length;
        while (items > 0) {
            // Create a bulk item of deletion
            BulkRequestBuilder bulk = getClient().prepareBulk();
            search.getHits().forEach(new Consumer<SearchHit>() {
                @Override
                public void accept(SearchHit t) {
                    bulk.add(getClient().prepareDelete(Config.observationsIndex(), OBJECT_NAME, t.getId()).request());
                }
            });
            // Execute the bulk
            bulk.execute();
            // Get the next page
            search = client.prepareSearchScroll(search.getScrollId()).setScroll(scroll).get();
            items = search.getHits().hits().length;
        }
    }

    @Override
    public boolean write(String uuid, ObservationJson observation) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                if (src != null) {
                    return new JsonPrimitive(src.getTime());
                }
                return null;
            }
        }).create();

        try {
            String document = gson.toJson(observation);
            document = new StringBuilder(DOC_PREFIX).append(document).append(DOC_SUFFIX).toString();

            UpdateRequest updateRequest = new UpdateRequest(Config.observationsIndex(), OBJECT_NAME, uuid);
            updateRequest.doc(document).upsert(document);

            getClient().update(updateRequest).get();
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
