package fr.ifremer.sensornanny.sync.cache;

import fr.ifremer.sensornanny.sync.config.Config;

public abstract class AbstractCacheManager<K, V> {

    private SimpleLRUCache<K, V> cache = new SimpleLRUCache<>(Config.cacheSize());

    /**
     * retrieve the data using the key
     * 
     * @param key search object
     * @return data
     */
    protected abstract V read(K key);

    /**
     * Get the data from cache if exist, otherwise call the read method
     * 
     * @param key identifier of the object
     * @return value data identified by the key
     */
    public V getData(K key) {
        V result = cache.get(key);
        if (result == null) {
            result = read(key);
            cache.put(key, result);
        }
        return result;
    }
}
