package io.github.sinri.keel.integration.redis;

import io.github.sinri.keel.core.cache.KeelAsyncCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * @since 3.0.5
 */
public class KeelAsyncCacheWithRedis implements KeelAsyncCacheInterface<String, String> {
    public static final long DEFAULT_LIFE_IN_SECONDS = 60;
    private final RedisKit redisKit;

    public KeelAsyncCacheWithRedis(String redisInstanceKey) {
        this.redisKit = new RedisKit(redisInstanceKey);
    }

    @Override
    public Future<Void> save(@Nonnull String key, String value, long lifeInSeconds) {
        return this.redisKit.setScalarToKeyForSeconds(key, value, Math.toIntExact(lifeInSeconds));
    }

    @Nonnull
    @Override
    public Future<String> read(@Nonnull String key) {
        return this.redisKit.getString(key)
                            .compose(value -> {
                                if (Objects.isNull(value)) {
                                    return Future.failedFuture(new NotCached("Value is null"));
                                }
                                return Future.succeededFuture(value);
                            });
    }

    @Nonnull
    @Override
    public Future<Void> save(@Nonnull String s, @Nullable String s2) {
        return save(s, s2, DEFAULT_LIFE_IN_SECONDS);
    }

    @Nonnull
    @Override
    public Future<String> read(@Nonnull String key, String fallbackValue) {
        return this.read(key)
                   .compose(s -> Future.succeededFuture(Objects.requireNonNullElse(s, fallbackValue)), throwable -> Future.succeededFuture(fallbackValue));
    }

    @Override
    public Future<String> read(@Nonnull String key, Function<String, Future<String>> generator, long lifeInSeconds) {
        return this.read(key).compose(s -> {
                       Objects.requireNonNull(s);
                       return Future.succeededFuture(s);
                   })
                   .recover(throwable -> generator.apply(key)
                                                  .compose(v -> save(key, v, lifeInSeconds)
                                                          .recover(saveFailed -> Future.succeededFuture())
                                                          .compose(anyway -> Future.succeededFuture(v))));
    }

    @Override
    public Future<Void> remove(@Nonnull String key) {
        return redisKit.deleteKey(key).compose(x -> Future.succeededFuture());
    }

    @Override
    public Future<Void> removeAll() {
        // 似乎可以使用
        // FLUSHDB [ASYNC]
        // 清空当前 select 数据库中的所有 key。
        // 但看起来比较危险
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> cleanUp() {
        // redis 自带这个机制
        return Future.succeededFuture();
    }

    @Override
    public Future<Set<String>> getCachedKeySet() {
        // KEYS pattern
        // Redis KEYS 命令用于查找所有匹配给定模式 pattern 的 key 。
        // 尽管这个操作的时间复杂度是 O(N)，但是常量时间相当小。
        // 但看起来比较危险
        throw new UnsupportedOperationException();
    }
}
