package com.nuraly.keycloak.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class CacheService {

    private static final Logger LOG = Logger.getLogger(CacheService.class);

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ObjectMapper objectMapper;

    private ValueCommands<String, String> commands;

    @jakarta.annotation.PostConstruct
    void init() {
        this.commands = redisDataSource.value(String.class);
    }

    public <T> void set(String key, T value, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(value);
            commands.setex(key, ttlSeconds, json);
        } catch (Exception e) {
            LOG.warn("Failed to cache value for key: " + key, e);
        }
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            String json = commands.get(key);
            if (json != null) {
                T value = objectMapper.readValue(json, clazz);
                return Optional.of(value);
            }
        } catch (Exception e) {
            LOG.warn("Failed to retrieve cached value for key: " + key, e);
        }
        return Optional.empty();
    }

    public void delete(String key) {
        try {
            redisDataSource.key().del(key);
        } catch (Exception e) {
            LOG.warn("Failed to delete cached value for key: " + key, e);
        }
    }
}
