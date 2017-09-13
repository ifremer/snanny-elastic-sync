package fr.ifremer.sensornanny.sync.dao;

/**
 *
 */
public interface ElasticSearchBulkProcessor {

    /**
     * Close the current bulk
     */
    void flush();
}
