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

import java.util.logging.Logger;

import static fr.ifremer.sensornanny.sync.constant.ObservationsFields.SNANNY_OBSERVATIONS;
import static fr.ifremer.sensornanny.sync.constant.ObservationsFields.SNANNY_SYSTEMS;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.manager.NodeManager;

/**
 * Created by asi on 29/09/16.
 */
public class ElasticMapping {

    private static final Logger LOGGER = Logger.getLogger(ElasticMapping.class.getName());

    protected void createMapping() throws IOException, ExecutionException, InterruptedException {
        create("mapping-observations.json", Config.observationsIndex(), SNANNY_OBSERVATIONS);
        create("mapping-systems.json", Config.systemsIndex(), SNANNY_SYSTEMS);
    }

    private void create(String fileName, String indexName, String typeName) throws IOException, ExecutionException, InterruptedException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);

        String source = IOUtils.toString(stream);

        IndicesExistsResponse timelineIndiceExist = getIndicesAdminClient().exists(new IndicesExistsRequest().indices(new String[]{indexName})).get();

        if (!timelineIndiceExist.isExists()) {
            getIndicesAdminClient().prepareCreate(indexName).execute().actionGet();
        }

        GetMappingsResponse reponse = getIndicesAdminClient().prepareGetMappings(indexName).setTypes(typeName).get();
        ImmutableOpenMap<String, MappingMetaData> mapIndex = reponse.getMappings().get(indexName);
        if (mapIndex == null || mapIndex.get(typeName) == null) {
            getIndicesAdminClient().preparePutMapping(indexName).setType(typeName).setSource(source,XContentType.JSON).get();
        }
    }

    private IndicesAdminClient getIndicesAdminClient() {
        return NodeManager.getInstance().getClient().admin().indices();
    }

}