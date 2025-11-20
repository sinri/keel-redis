package io.github.sinri.keel.integration.redis.cache;

import io.github.sinri.keel.core.cache.KeelAsyncCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.integration.redis.kit.RedisKit;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * 基于 Redis 的异步缓存实现。
 *
 * @since 5.0.0
 */
public class KeelAsyncCacheWithRedis implements KeelAsyncCacheInterface<String, String> {
    public static final long DEFAULT_LIFE_IN_SECONDS = 60;
    private final RedisKit redisKit;

    public KeelAsyncCacheWithRedis(String redisInstanceKey) {
        this.redisKit = new RedisKit(redisInstanceKey);
    }

    @Override
    public Future<Void> save(@NotNull String key, String value, long lifeInSeconds) {
        return this.redisKit.setScalarToKeyForSeconds(key, value, Math.toIntExact(lifeInSeconds));
    }

    @NotNull
    @Override
    public Future<String> read(@NotNull String key) {
        return this.redisKit.getString(key)
                            .compose(value -> {
                                if (Objects.isNull(value)) {
                                    return Future.failedFuture(new NotCached("Value is null"));
                                }
                                return Future.succeededFuture(value);
                            });
    }

    @NotNull
    @Override
    public Future<Void> save(@NotNull String s, @Nullable String s2) {
        return save(s, s2, DEFAULT_LIFE_IN_SECONDS);
    }

    @NotNull
    @Override
    public Future<String> read(@NotNull String key, String fallbackValue) {
        return this.read(key)
                   .compose(s -> Future.succeededFuture(Objects.requireNonNullElse(s, fallbackValue)), throwable -> Future.succeededFuture(fallbackValue));
    }

    @Override
    public Future<String> read(@NotNull String key, Function<String, Future<String>> generator, long lifeInSeconds) {
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
    public Future<Void> remove(@NotNull String key) {
        return redisKit.deleteKey(key).compose(x -> Future.succeededFuture());
    }

    /**
     * 这是一个危险的动作，因此不支持。
     * <p>
     * 似乎可以使用 {@code FLUSHDB [ASYNC]} 清空当前 select 数据库中的所有 key，但看起来比较危险。
     *
     * @throws UnsupportedOperationException 这是一个不支持的危险动作
     */
    @Override
    public Future<Void> removeAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * 由于 redis 自带这个机制，所以不需要手工操作。
     *
     * @return 立即返回的异步成功结果
     */
    @Override
    public Future<Void> cleanUp() {
        return Future.succeededFuture();
    }

    /**
     * 这是一个危险的动作，因此不支持。
     * <p>
     * Redis 的 {@code KEYS pattern} 命令用于查找所有匹配给定模式 pattern 的 key 。
     * 尽管这个操作的时间复杂度是 O(N)，但是常量时间相当小。
     * 但看起来比较危险。
     *
     * @throws UnsupportedOperationException 这是一个不支持的危险动作
     */
    @Override
    public Future<Set<String>> getCachedKeySet() {
        throw new UnsupportedOperationException();
    }
}
