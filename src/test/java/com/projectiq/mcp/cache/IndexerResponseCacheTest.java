package com.projectiq.mcp.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IndexerResponseCache}.
 * Covers cache hit, miss, expiration, disabled state, thread safety, and configuration.
 */
class IndexerResponseCacheTest {

    private IndexerResponseCache cache;
    private CacheProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CacheProperties();
        properties.setEnabled(true);
        properties.setTtlSeconds(300);
        properties.setMaxSize(1000);
        cache = new IndexerResponseCache(properties);
    }

    @AfterEach
    void tearDown() {
        cache.shutdown();
    }

    @Test
    void cacheHit_returnsCachedValue() {
        String key = "test-key";
        String value = "test-value";

        cache.put(key, value);
        Object result = cache.get(key);

        assertThat(result).isEqualTo(value);
    }

    @Test
    void cacheMiss_returnsNull() {
        String key = "non-existent-key";
        Object result = cache.get(key);

        assertThat(result).isNull();
    }

    @Test
    void cacheExpiration_returnsNullAfterTtl() throws Exception {
        properties.setTtlSeconds(1); // 1 second TTL

        String key = "expiring-key";
        String value = "expiring-value";

        cache.put(key, value);

        // Should still be in cache
        assertThat(cache.get(key)).isEqualTo(value);

        // Wait for expiration
        Thread.sleep(1100);

        // Should be null after expiration
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void cacheDisabled_returnsNullAlways() {
        properties.setEnabled(false);
        // Recreate cache with disabled property
        cache.shutdown();
        cache = new IndexerResponseCache(properties);

        String key = "disabled-key";
        String value = "disabled-value";

        cache.put(key, value);

        // Should always return null when disabled
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void putNullValue_doesNotCache() {
        String key = "null-key";

        cache.put(key, null);

        assertThat(cache.get(key)).isNull();
    }

    @Test
    void remove_evictsEntry() {
        String key = "remove-key";
        String value = "remove-value";

        cache.put(key, value);
        assertThat(cache.get(key)).isEqualTo(value);

        cache.remove(key);
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clear_evictsAllEntries() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        assertThat(cache.size()).isEqualTo(3);

        cache.clear();

        assertThat(cache.size()).isZero();
        assertThat(cache.get("key1")).isNull();
        assertThat(cache.get("key2")).isNull();
        assertThat(cache.get("key3")).isNull();
    }

    @Test
    void size_returnsCorrectCount() {
        assertThat(cache.size()).isZero();

        cache.put("key1", "value1");
        assertThat(cache.size()).isEqualTo(1);

        cache.put("key2", "value2");
        assertThat(cache.size()).isEqualTo(2);

        cache.remove("key1");
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void isEnabled_reflectsConfiguration() {
        assertThat(cache.isEnabled()).isTrue();

        properties.setEnabled(false);
        cache.shutdown();
        cache = new IndexerResponseCache(properties);

        assertThat(cache.isEnabled()).isFalse();
    }

    @Test
    void maxSize_evictsOldestWhenFull() {
        properties.setMaxSize(2);
        cache.shutdown();
        cache = new IndexerResponseCache(properties);

        cache.put("key1", "value1");
        cache.put("key2", "value2");
        assertThat(cache.size()).isEqualTo(2);

        // Adding a third entry should evict one
        cache.put("key3", "value3");
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void updateExistingEntry_doesNotCountTowardsMaxSize() {
        properties.setMaxSize(2);
        cache.shutdown();
        cache = new IndexerResponseCache(properties);

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Update existing entry - should not cause eviction
        cache.put("key1", "updated-value1");
        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get("key1")).isEqualTo("updated-value1");
    }

    @Test
    void threadSafety_concurrentAccess() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String key = "key-" + threadId + "-" + i;
                        String value = "value-" + threadId + "-" + i;

                        // Write
                        cache.put(key, value);

                        // Read
                        Object retrieved = cache.get(key);
                        if (retrieved == null) {
                            // Could be evicted, that's acceptable
                            continue;
                        }
                        if (!value.equals(retrieved)) {
                            errors.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(errors.get()).as("No thread safety errors expected").isZero();
    }

    @Test
    void concurrentReads_doNotBlockEachOther() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successes = new AtomicInteger(0);

        // Pre-populate cache
        for (int i = 0; i < 100; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        Object result = cache.get("key-" + i);
                        if (result != null && result.equals("value-" + i)) {
                            successes.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // At least some reads should succeed (ConcurrentHashMap ensures visibility)
        assertThat(successes.get()).isPositive();
    }

    @Test
    void getAfterEvictExpiredEntries_returnsNull() throws Exception {
        properties.setTtlSeconds(1);
        cache.shutdown();
        cache = new IndexerResponseCache(properties);

        cache.put("exp-key", "exp-value");
        assertThat(cache.get("exp-key")).isEqualTo("exp-value");

        // Wait for expiration
        Thread.sleep(1100);

        // Trigger eviction by getting expired entry
        assertThat(cache.get("exp-key")).isNull();
    }

    @Test
    void multipleKeys_cacheIndependently() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        assertThat(cache.get("key1")).isEqualTo("value1");
        assertThat(cache.get("key2")).isEqualTo("value2");
        assertThat(cache.get("key3")).isEqualTo("value3");

        // Remove one should not affect others
        cache.remove("key2");

        assertThat(cache.get("key1")).isEqualTo("value1");
        assertThat(cache.get("key2")).isNull();
        assertThat(cache.get("key3")).isEqualTo("value3");
    }

    @Test
    void cacheKeyWithSpecialCharacters() {
        String key = "repo:my-project/branch:main/query:test+query";
        String value = "special-chars-value";

        cache.put(key, value);
        Object result = cache.get(key);

        assertThat(result).isEqualTo(value);
    }
}