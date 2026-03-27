package io.github.sinri.keel.integration.redis.kit;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Closeable;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import org.jspecify.annotations.NullMarked;


/**
 * Redis API 工具
 *
 * @since 5.0.0
 */
@NullMarked
public class RedisKit implements Closeable,
        RedisScalarMixin, RedisListMixin, RedisBitMixin, RedisHashMixin, RedisSetMixin, RedisOrderedSetMixin {

    private final Redis client;
    // RedisAPI wrapping the pooled Redis client — connection management is automatic.
    // Do NOT call redisAPI.close() during normal operation; only close in RedisKit.close().
    private final RedisAPI redisAPI;

    public RedisKit(Keel keel, RedisConfig redisConfig) throws NotConfiguredException {
        this.client = Redis.createClient(keel, redisConfig.toRedisOptions());
        this.redisAPI = RedisAPI.api(this.client);
    }

    public Redis getClient() {
        return client;
    }

    public RedisAPI getRedisAPI() {
        return redisAPI;
    }

    public Future<Void> close() {
        this.redisAPI.close();
        return this.client.close();
    }

    @Override
    public void close(Completable<Void> completion) {
        this.client.close().onComplete(completion);
    }

}
