package fr.ifremer.sensornanny.sync.manager;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import fr.ifremer.sensornanny.sync.config.Config;

/**
 * NodeManager elasticsearch, allow to create a transportClient on the defined
 * clusters
 * 
 * @author athorel
 *
 */
public final class NodeManager {

    private static final String CLIENT_TRANSPORT_SNIFF = "client.transport.sniff";

    private static final String CLUSTER_NAME = "cluster.name";

    private static final Logger LOGGER = Logger.getLogger(NodeManager.class.getName());

    private static TransportClient client;

    private static NodeManager instance;

    private NodeManager() {
        extractClientSettings();
    }

    public static NodeManager getInstance() {
        if (instance == null) {
            instance = new NodeManager();
        }
        return instance;
    }

    public static void init() {
        LOGGER.log(Level.INFO, "Connecting to ElasticSearch Cluster");
        extractClientSettings();
    }

    public static void extractClientSettings() {
        Settings clientSettings = ImmutableSettings.settingsBuilder().put(CLUSTER_NAME, Config.clusterName()).put(
                CLIENT_TRANSPORT_SNIFF, true).build();

        client = new TransportClient(clientSettings);
        String[] nodes = Config.clusterHosts();
        for (String host : nodes) {
            client.addTransportAddress(new InetSocketTransportAddress(host, Config.clusterPort()));
        }
    }

    /**
     * Get the transport client for searchs
     * 
     * @return transport client configured with properties
     */
    public Client getClient() {
        return client;
    }

    public static void destroy() {
        if (client != null) {
            client.close();
        }
    }

}
