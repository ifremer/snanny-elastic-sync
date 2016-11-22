package fr.ifremer.sensornanny.sync.dao.impl;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.*;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Ancestor;
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

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IObservationDao;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.manager.NodeManager;

import static fr.ifremer.sensornanny.sync.constant.ObservationsFields.*;

/**
 * Implementation of elastic Dao
 * 
 * @author athorel
 *
 */
public class ObservationDaoImpl implements IObservationDao {

    private static final Logger LOGGER = Logger.getLogger(ObservationDaoImpl.class.getName());
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
                    bulk.add(client.prepareDelete(Config.observationsIndex(), SNANNY_OBSERVATIONS, t.getId()).request());
                }
            });
            // Execute the bulk
            bulk.execute();
            // Get the next page
            search = client.prepareSearch(Config.observationsIndex()).setQuery(QueryBuilders.wildcardQuery(
                    SNANNY_UUID, uuid + WILDCARDS)).setScroll(scroll).setSize(NUMBER_OF_ITEMS_PER_DELETION).get();
            items = search.getHits().hits().length;
        }
    }

    @Override
    public boolean write(String uuid, ObservationJson observation) {

        try {
            JsonObject item = new JsonObject();
            item.addProperty(SNANNY_DEPLOYMENTID, observation.getDeploymentId());
            item.addProperty(SNANNY_UUID, uuid);
            item.addProperty(SNANNY_RESULTTIMESTAMP, observation.getResultTimestamp().getTime());
            item.addProperty(SNANNY_RESULTFILE, observation.getResult());
            item.addProperty(SNANNY_UPDATETIMESTAMP, observation.getUpdateTimestamp().getTime());
            JsonArray arrAncestors = new JsonArray();
            observation.getAncestors().stream().forEach(a -> arrAncestors.add(transformAncestor(a)));
            item.add(SNANNY_ANCESTORS, arrAncestors);
            JsonObject access = new JsonObject();
            JsonArray accessAuth = new JsonArray();
            if(observation.getPermission().getAuthorized() != null) {
                observation.getPermission().getAuthorized().stream().forEach(auth -> accessAuth.add(new JsonPrimitive(auth)));
            }
            access.add(SNANNY_ACCESS_AUTH, accessAuth);
            access.addProperty(SNANNY_ACCESS_TYPE, observation.getPermission().getStatus());
            item.add(SNANNY_ACCESS, access);
            item.addProperty(SNANNY_AUTHOR, observation.getAuthor());
            item.addProperty(SNANNY_DEPTH, observation.getDepth());
            item.addProperty(SNANNY_NAME, observation.getName());
            item.addProperty(SNANNY_COORDINATES, observation.getCoordinates());

            UpdateRequest updateRequest = new UpdateRequest(Config.observationsIndex(), SNANNY_OBSERVATIONS, uuid);
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

    private JsonObject transformAncestor(Ancestor ancestorObs) {
        JsonObject ancestor = new JsonObject();
        ancestor.addProperty(SNANNY_ANCESTOR_DEPLOYMENTID, ancestorObs.getDeploymentId());
        ancestor.addProperty(SNANNY_ANCESTOR_NAME, ancestorObs.getName());
        ancestor.addProperty(SNANNY_ANCESTOR_UUID, ancestorObs.getUuid());
        ancestor.addProperty(SNANNY_ANCESTOR_DESCRIPTION, ancestorObs.getDescription());
        ancestor.addProperty(SNANNY_ANCESTOR_TERMS, ancestorObs.getTerms().toString());
        return ancestor;
    }

    private Client getClient() {
        return NodeManager.getInstance().getClient();
    }

}
