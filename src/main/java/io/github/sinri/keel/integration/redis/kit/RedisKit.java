package io.github.sinri.keel.integration.redis.kit;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.redis.client.Redis;
import org.jetbrains.annotations.NotNull;



/**
 * Redis API 工具
 *
 * @since 5.0.0
 */
public class RedisKit implements RedisScalarMixin, RedisListMixin, RedisBitMixin, RedisHashMixin, RedisSetMixin,
        RedisOrderedSetMixin {
    @NotNull
    private final Redis client;
    @NotNull
    private final Keel keel;

    public RedisKit(@NotNull Keel keel, @NotNull RedisConfig redisConfig) throws NotConfiguredException {
        this.client = Redis.createClient(keel.getVertx(), redisConfig.toRedisOptions());
        this.keel = keel;
    }

    public @NotNull Keel getKeel() {
        return keel;
    }

    public @NotNull Redis getClient() {
        return client;
    }

    public void close() {
        this.client.close();
    }

}
