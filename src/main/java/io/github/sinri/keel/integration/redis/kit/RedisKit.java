package io.github.sinri.keel.integration.redis.kit;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Closeable;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
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

    public RedisKit(Keel keel, RedisConfig redisConfig) throws NotConfiguredException {
        this.client = Redis.createClient(keel, redisConfig.toRedisOptions());
    }

    public Redis getClient() {
        return client;
    }

    public Future<Void> close() {
        return this.client.close();
    }

    @Override
    public void close(Completable<Void> completion) {
        this.client.close().onComplete(completion);
    }

}
