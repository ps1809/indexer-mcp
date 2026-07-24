package com.projectiq.mcp.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe, in-memory cache for Indexer responses.
 * Supports configurable TTL, maximum size, and automatic expiration of stale entries.
 * Uses a background scheduler to periodically evict expired entries.
 */
public class IndexerResponseCache {

    private static final Logger logger = LoggerFactory.getLogger(IndexerResponseCache.class);

    private final CacheProperties properties;
    private final Map<String, CacheEntry> cache;
    private final ReentrantReadWriteLock lock;
    private final ScheduledExecutorService cleanupExecutor;

    /**
     * Creates a new cache with the given properties.
     *
     * @param properties cache configuration
     */
    public IndexerResponseCache(CacheProperties properties) {
        this.properties = properties;
        this.cache = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        // Schedule periodic cleanup of expired entries
        long cleanupInterval = Math.max(properties.getTtlSeconds() / 2, 10);
        this.cleanupExecutor.scheduleAtFixedRate(
                this::evictExpiredEntries,
                cleanupInterval,
                cleanupInterval,
                TimeUnit.SECONDS
        );

        logger.info("IndexerResponseCache initialized: enabled={}, ttl={}s, maxSize={}",
                properties.isEnabled(), properties.getTtlSeconds(), properties.getMaxSize());
    }

    /**
     * Retrieves a cached response for the given key.
     *
     * @param key the cache key
     * @return the cached response, or null if not present or expired
     */
    public Object get(String key) {
        if (!properties.isEnabled()) {
            return null;
        }

        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                logger.debug("Cache miss for key: {}", key);
                return null;
            }

            if (entry.isExpired()) {
                logger.debug("Cache entry expired for key: {}", key);
                cache.remove(key);
                return null;
            }

            logger.debug("Cache hit for key: {}", key);
            return entry.getValue();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Stores a response in the cache.
     *
     * @param key   the cache key
     * @param value the response to cache
     */
    public void put(String key, Object value) {
        if (!properties.isEnabled()) {
            return;
        }

        if (value == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            // Evict oldest entry if at capacity
            if (cache.size() >= properties.getMaxSize() && !cache.containsKey(key)) {
                evictOneEntry();
            }

            CacheEntry entry = new CacheEntry(value, properties.getTtlSeconds());
            cache.put(key, entry);
            logger.debug("Cached response for key: {}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a specific entry from the cache.
     *
     * @param key the cache key to remove
     */
    public void remove(String key) {
        lock.writeLock().lock();
        try {
            cache.remove(key);
            logger.debug("Removed cache entry for key: {}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            logger.debug("Cache cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the current number of entries in the cache.
     *
     * @return cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Checks if the cache is enabled.
     *
     * @return true if cache is enabled
     */
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Evicts all expired entries from the cache.
     */
    private void evictExpiredEntries() {
        lock.writeLock().lock();
        try {
            int before = cache.size();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            int evicted = before - cache.size();
            if (evicted > 0) {
                logger.debug("Evicted {} expired cache entries", evicted);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Evicts a single entry from the cache when at capacity.
     * Uses a simple approach: removes the first expired entry found, or the oldest entry.
     */
    private void evictOneEntry() {
        // First try to remove an expired entry
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                logger.debug("Evicted expired entry: {}", entry.getKey());
                return;
            }
        }

        // Otherwise remove the first entry (oldest insertion order is not guaranteed
        // with ConcurrentHashMap, but this is acceptable for MVP)
        Map.Entry<String, CacheEntry> firstEntry = cache.entrySet().iterator().next();
        if (firstEntry != null) {
            cache.remove(firstEntry.getKey());
            logger.debug("Evicted oldest entry: {}", firstEntry.getKey());
        }
    }

    /**
     * Shuts down the cleanup executor. Should be called when the cache is no longer needed.
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Internal cache entry with value and expiration timestamp.
     */
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;

        CacheEntry(Object value, long ttlSeconds) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        Object getValue() {
            return value;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}