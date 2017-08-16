package fr.ifremer.sensornanny.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentType;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.manager.NodeManager;

/**
 * Created by asi on 29/09/16.
 */
public class ElasticMapping {

    private static final String SNANNY_OBSERVATIONS = "snanny-observations";

    protected void createMapping() throws IOException, ExecutionException, InterruptedException {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("mapping.json");
        String index = Config.observationsIndex();

        String source = IOUtils.toString(stream);
        IndicesAdminClient indices = NodeManager.getInstance().getClient().admin().indices();
        IndicesExistsResponse timelineIndiceExist = indices.exists(new IndicesExistsRequest().indices(new String[]{index})).get();

        if (!timelineIndiceExist.isExists()) {
            indices.prepareCreate(index).execute().actionGet();
        }

        GetMappingsResponse reponse = indices.prepareGetMappings(index).setTypes(SNANNY_OBSERVATIONS).get();
        ImmutableOpenMap<String, MappingMetaData> mapIndex = reponse.getMappings().get(index);
        if (mapIndex == null || mapIndex.get(SNANNY_OBSERVATIONS) == null) {
            indices.preparePutMapping(index).setType(SNANNY_OBSERVATIONS).setSource(source, XContentType.JSON).get();
        }
    }
}