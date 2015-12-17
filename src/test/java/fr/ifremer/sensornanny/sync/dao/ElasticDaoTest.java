package fr.ifremer.sensornanny.sync.dao;

import java.util.Arrays;
import java.util.Calendar;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoHashGridBuilder;
import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.impl.ElasticDaoImpl;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Ancestor;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Coordinates;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.manager.NodeManager;

public class ElasticDaoTest extends UnitTest {

    public IElasticDao dao = new ElasticDaoImpl();

    @Test
    public void testGetFiles() {

        Ancestor createAncestor = createAncestor("001", "Navire Ifremer", "Profiling IFR");
        Ancestor createAncestor2 = createAncestor("002", "CINNA", "Cinna desc");

        for (int i = 0; i < 100; i++) {

            ObservationJson observation = new ObservationJson();
            observation.setUuid("abcdefg-" + String.format("%010d", i));
            observation.setAuthor("athorel");
            observation.setDescription("description bob");
            observation.setName("profile from float 4900682 on 1280067060");
            observation.setResultTimestamp(Calendar.getInstance().getTime());
            observation.setUpdateTimestamp(Calendar.getInstance().getTime());
            observation.setFamily("family");

            Coordinates coordinates = new Coordinates();
            double caze = Math.random();
            boolean less = caze > 0.5;
            double lat = caze * 90;
            coordinates.setLat(less ? -lat : lat);
            double lon = caze * 180;
            coordinates.setLon(less ? -lon : lon);
            observation.setDepth(-(caze * 1800));
            observation.setCoordinates(coordinates);

            boolean newRand = Math.random() > 0.5;
            observation.setAncestors(Arrays.asList((newRand ? createAncestor : createAncestor2)));

            boolean write = dao.write(i + "", observation);

            Assert.assertEquals(true, write);
        }
        dao.delete("abcdefg-");

    }

    private Ancestor createAncestor(String id, String name, String desc) {
        Ancestor ancestor = new Ancestor();
        ancestor.setUuid(id);
        ancestor.setName(name);
        ancestor.setDescription(desc);
        ancestor.setKeywords(Arrays.asList("key0", "key1"));
        ancestor.setTerms(Arrays.asList("term0", "term1"));
        return ancestor;
    }

    @Test
    public void testAggregat() {

        SearchRequestBuilder searchRequest = NodeManager.getInstance().getClient().prepareSearch(Config
                .observationsIndex());

        GeoHashGridBuilder geohashAggregation = AggregationBuilders.geohashGrid("agGeo").precision(4).field(
                "snanny-coordinates");

        searchRequest.addAggregation(geohashAggregation);

        searchRequest.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        // Get aggregation Map only
        SearchResponse result = searchRequest.setSize(0).execute().actionGet();
        Aggregation aggregation = result.getAggregations().get("agGeo");
        System.out.println(aggregation.getName());

    }

}
