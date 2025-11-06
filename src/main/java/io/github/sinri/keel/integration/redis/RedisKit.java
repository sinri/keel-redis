package io.github.sinri.keel.integration.redis;

import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.integration.redis.mixin.*;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

import javax.annotation.Nonnull;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.5
 */
public class RedisKit implements RedisScalarMixin, RedisListMixin, RedisBitMixin, RedisHashMixin, RedisSetMixin,
        RedisOrderedSetMixin {
    private final Redis client;

    public RedisKit(String redisInstanceKey) {
        RedisConfig redisConfig = new RedisConfig(Objects.requireNonNull(
                Keel.getConfiguration().extract("redis", redisInstanceKey)));
        this.client = Redis.createClient(Keel.getVertx(), redisConfig.toRedisOptions());
    }

    public Redis getClient() {
        return client;
    }

    /**
     * @since 3.2.18
     */
    public void close() {
        this.client.close();
    }

    /**
     * A configuration wrapper for Redis connection settings.
     * This class encapsulates all configuration parameters needed for establishing
     * and managing Redis client connections.
     *
     * <p>
     * In properties format configure file, a redis instance block like:
     * </p>
     * <p>
     * redis.INSTANCE_NAME.url=...<br/>
     * redis.INSTANCE_NAME.maxPoolSize=16<br/>
     * redis.INSTANCE_NAME.maxWaitingHandlers=32<br/>
     * redis.INSTANCE_NAME.maxPoolWaiting=24<br/>
     * redis.INSTANCE_NAME.poolCleanerInterval=5000<br/>
     * </p>
     */
    private static class RedisConfig extends KeelConfigElement {

        /**
         * Constructs a RedisConfig instance from an existing configuration element.
         *
         * @param another The source configuration element to copy from
         * @throws NullPointerException If another is null
         */
        public RedisConfig(@Nonnull KeelConfigElement another) {
            super(another);
        }

        /**
         * Gets the Redis connection URL.
         * The URL format should be {@code redis://[:password@]host[:port][/db-number]}
         *
         * @return The Redis connection URL
         * @throws NullPointerException If the URL is not configured
         */
        @Nonnull
        public String getUrl() {
            return Objects.requireNonNull(readString("url", null));
        }

        /**
         * Gets the maximum number of connections in the Redis connection pool.
         * Maximum number of concurrent connections maintained in the connection pool.
         * Higher values allow more concurrent operations but consume more resources.
         * For high-concurrency environments, increase this value based on your
         * application needs and Redis server
         * capacity.
         *
         * @return The maximum pool size, defaults to 16 if not specified
         */
        public int getMaxPoolSize() {
            return readInteger("maxPoolSize", 16);
        }

        /**
         * Gets the maximum number of waiting handlers when the connection pool is full.
         * Maximum number of operation handlers that can wait for a connection when all
         * connections are busy.
         * This prevents unbounded growth of waiting operations when Redis is under
         * heavy load or unresponsive.
         *
         * @return The maximum number of waiting handlers, defaults to 32 if not
         *         specified
         */
        public int getMaxWaitingHandlers() {
            return readInteger("maxWaitingHandlers", 32);
        }

        /**
         * Gets the maximum number of requests waiting for a connection when the pool
         * reaches its maximum size.
         * Maximum number of connection requests that can be queued when the pool has
         * reached maxPoolSize.
         * Prevents excessive memory usage during connection spikes by limiting the
         * queue size.
         *
         * @return The maximum number of waiting requests, defaults to 24 if not
         *         specified
         */
        public int getMaxPoolWaiting() {
            return readInteger("maxPoolWaiting", 24);
        }

        /**
         * Gets the interval in milliseconds for cleaning idle connections in the pool.
         * Time interval in milliseconds for cleaning idle connections from the pool.
         * Lower values keep the pool size smaller but add cleaning overhead; higher
         * values might leave unused
         * connections open longer.
         *
         * @return The pool cleaner interval in milliseconds, defaults to 5000 if not
         *         specified
         */
        public int getPoolCleanerInterval() {
            return readInteger("poolCleanerInterval", 5000);
        }

        /**
         * Converts this configuration into a Vert.x RedisOptions object.
         * Applies all configured Redis connection parameters.
         *
         * @return A new RedisOptions object configured with the settings from this
         *         config
         */
        public RedisOptions toRedisOptions() {
            return new RedisOptions()
                    .setConnectionString(getUrl())
                    .setMaxPoolSize(getMaxPoolSize())
                    .setMaxWaitingHandlers(getMaxWaitingHandlers())
                    .setMaxPoolWaiting(getMaxPoolWaiting())
                    .setPoolCleanerInterval(getPoolCleanerInterval());
        }
    }
}
