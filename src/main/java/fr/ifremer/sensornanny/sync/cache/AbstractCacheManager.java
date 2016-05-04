package fr.ifremer.sensornanny.sync.cache;

import fr.ifremer.sensornanny.sync.config.Config;

import java.util.Date;

/**
 * Abstract cache manager that allow to retrieve information, from cache if present or from the service if no present
 * This class use a {@link SimpleLRUCache} configured by {@link Config#cacheSize()} which store a limited number of
 * object V representing by the key K
 * <p>
 * Usage : Call getData(K key) : Will check the cache before
 * -> If object not in cache, call method read and put result in cache
 * -> Otherwise returned the cached data
 * <p>
 * To avoid large memory usage the cache is configured to clean old cached item if {@link Config#cacheSize()} is reached
 *
 * @param <K> Type of the key
 * @param <V> Type of the object
 * @author athorel
 */
public abstract class AbstractCacheManager<K, V> {

    /**
     * Simple LRU cache
     */
    private SimpleLRUCache<K, V> cache = new SimpleLRUCache<>(Config.cacheSize());
    private SimpleLRUCache<SmlWithDateCacheKey, V> cacheByDate = new SimpleLRUCache<>(Config.cacheSize());

    /**
     * retrieve the data using the key
     *
     * @param key search object
     * @return data
     */
    protected abstract V read(K key);

    /**
     * retrieve the data using the key
     *
     * @param key       search object
     * @param startTime startTime search
     * @param endTime   endTime search
     * @return data
     */
    protected abstract V read(K key, Date startTime, Date endTime);

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

    /**
     * Get the data from cache if exist, otherwise call the read method
     *
     * @param key       search object
     * @param startTime startTime search
     * @param endTime   endTime search
     * @return data
     */
    public V getData(K key, Date startTime, Date endTime) {
        SmlWithDateCacheKey<K> cacheKey = new SmlWithDateCacheKey<>();
        cacheKey.setKey(key);
        cacheKey.setStartTime(startTime);
        cacheKey.setEndTime(endTime);
        V result = cacheByDate.get(cacheKey);
        if (result == null) {
            result = read(key, startTime, endTime);
            cacheByDate.put(cacheKey, result);
        }
        return result;
    }
}
