package fr.ifremer.sensornanny.sync.config;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.ifremer.sensornanny.sync.util.PropertyLoader;

/**
 * Class that allow access to properties
 * 
 * @author athorel
 *
 */
public final class Config {

    /** Property file */
    private static final String CONFIGURATION_FILENAME = "application.properties";

    private static final Logger logger = Logger.getLogger(Config.class.getName());

    private static Config instance = new Config();

    private Properties properties;

    private Config() {
        properties = PropertyLoader.load(CONFIGURATION_FILENAME);
    }

    /**
     * Hosts of the nodes in a elasticSearch cluster
     * 
     * @return array of hosts
     */
    public static String[] clusterHosts() {
        return get("es.cluster.nodes").split(",");
    }

    /***
     * Name of the cluster elasticSearch (this configuration allow autodiscover nodes)
     * 
     * @return name of the cluster
     */
    public static String clusterName() {
        return get("es.cluster.name");
    }

    /**
     * Transport port of the cluster (default is 9300)
     * 
     * @return transport port of the nodes
     */
    public static int clusterPort() {
        return getInt("es.cluster.port", 9300);
    }

    /**
     * Index elastic search on observations
     * 
     * @return indexName
     */
    public static String observationsIndex() {
        return get("es.index.observations");
    }

    /**
     * @return owncloud service endpoint url
     */
    public static String owncloudEndpoint() {
        return get("owncloud.endpoint");
    }

    /**
     * Owncloud services credentials
     * 
     * @return credentials authentifier
     */
    public static String owncloudCredentials() {
        return get("owncloud.credentials");
    }

    /**
     * Owncloud local storage root
     * 
     * @return local storage
     */
    public static String owncloudStorage() {
        return get("owncloud.storage");
    }

    /**
     * Number of thread used for synchronisation
     * 
     * @return number of thread
     */
    public static int threadNumbers() {
        return getInt("sync.process", 5);
    }

    /**
     * Get the cache size
     * 
     * @return number of item in cache
     */
    public static int cacheSize() {
        return getInt("sync.cacheSize", 10);
    }

    /**
     * Get the cache size
     * 
     * @return number of item in cache
     */
    public static int maxMemory() {
        return getInt("sync.maxMemory", 200);
    }

    public static File outputFolder() {
        return new File(get("sync.log.outputfolder"));
    }

    /**
     * Service endpoint to retrieve sml informations
     * 
     * @return service endpoint
     */
    public static String smlEndpoint() {
        return get("sml.endpoint");
    }

    /**
     * Service endpoint to retrieve tematres informations
     * 
     * @return service endpoint
     */
    public static String tematresEndpoint() {
        return get("tematres.endpoint");
    }

    /**
     * Get the cache size
     * 
     * @return number of item in cache
     */
    public static int syncModulo() {
        return getInt("sync.modulo", 100);
    }

    /**
     * Check properties
     */
    private void checkProperties() {
        if (properties == null) {
            String message = "Property file '" + CONFIGURATION_FILENAME + "' not initialized";
            logger.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        }
    }

    /**
     * Retrieve a String property
     * 
     * @param property property to retrieve
     * @return string value of the property
     */
    private static String get(String property) {
        instance.checkProperties();
        String value = instance.properties.getProperty(property);
        if (value == null) {
            String message = "Property named " + property + " not found in '" + CONFIGURATION_FILENAME + "'";
            logger.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        }
        return value;
    }

    /**
     * Retrieve an integer property
     * 
     * @param property property to retrieve
     * @param defaultValue default value if not found
     * @return integer value of the property if found, otherwise return the default value
     */
    private static int getInt(String property, int defaultValue) {
        try {
            String key = get(property);
            return Integer.parseInt(key);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

}
