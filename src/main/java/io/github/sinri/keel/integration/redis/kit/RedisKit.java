package io.github.sinri.keel.integration.redis.kit;

import io.github.sinri.keel.base.configuration.ConfigTree;
import io.vertx.redis.client.Redis;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * Redis API 工具
 *
 * @since 5.0.0
 */
public class RedisKit implements RedisScalarMixin, RedisListMixin, RedisBitMixin, RedisHashMixin, RedisSetMixin,
        RedisOrderedSetMixin {
    @NotNull
    private final Redis client;

    public RedisKit(@NotNull String redisInstanceKey) throws ConfigTree.NotConfiguredException {
        this(new RedisConfig(Objects.requireNonNull(
                Keel.getConfiguration().extract("redis", redisInstanceKey))));
    }

    public RedisKit(@NotNull RedisConfig redisConfig) throws ConfigTree.NotConfiguredException {
        this.client = Redis.createClient(Keel.getVertx(), redisConfig.toRedisOptions());
    }

    public @NotNull Redis getClient() {
        return client;
    }

    public void close() {
        this.client.close();
    }

}
