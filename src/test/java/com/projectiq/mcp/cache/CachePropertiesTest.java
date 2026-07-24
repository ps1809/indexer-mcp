package com.projectiq.mcp.cache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CacheProperties} configuration loading.
 */
class CachePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void defaultValues() {
        contextRunner.run(context -> {
            CacheProperties properties = context.getBean(CacheProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getTtlSeconds()).isEqualTo(300);
            assertThat(properties.getMaxSize()).isEqualTo(1000);
        });
    }

    @Test
    void customEnabled() {
        contextRunner
                .withPropertyValues("projectiq.cache.enabled=false")
                .run(context -> {
                    CacheProperties properties = context.getBean(CacheProperties.class);
                    assertThat(properties.isEnabled()).isFalse();
                });
    }

    @Test
    void customTtl() {
        contextRunner
                .withPropertyValues("projectiq.cache.ttl-seconds=600")
                .run(context -> {
                    CacheProperties properties = context.getBean(CacheProperties.class);
                    assertThat(properties.getTtlSeconds()).isEqualTo(600);
                });
    }

    @Test
    void customMaxSize() {
        contextRunner
                .withPropertyValues("projectiq.cache.max-size=500")
                .run(context -> {
                    CacheProperties properties = context.getBean(CacheProperties.class);
                    assertThat(properties.getMaxSize()).isEqualTo(500);
                });
    }

    @Test
    void allCustomValues() {
        contextRunner
                .withPropertyValues(
                        "projectiq.cache.enabled=false",
                        "projectiq.cache.ttl-seconds=120",
                        "projectiq.cache.max-size=50"
                )
                .run(context -> {
                    CacheProperties properties = context.getBean(CacheProperties.class);
                    assertThat(properties.isEnabled()).isFalse();
                    assertThat(properties.getTtlSeconds()).isEqualTo(120);
                    assertThat(properties.getMaxSize()).isEqualTo(50);
                });
    }

    @EnableConfigurationProperties(CacheProperties.class)
    static class TestConfig {
    }
}