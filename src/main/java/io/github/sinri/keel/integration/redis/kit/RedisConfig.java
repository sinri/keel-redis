package io.github.sinri.keel.integration.redis.kit;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.redis.client.RedisOptions;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * A configuration wrapper for Redis connection settings.
 * This class encapsulates all configuration parameters needed for establishing
 * and managing Redis client connections.
 *
 * <p>
 * In the properties format configure file, a redis instance block like:
 * </p>
 * <p>
 * redis.INSTANCE_NAME.url=...<br/>
 * redis.INSTANCE_NAME.maxPoolSize=16<br/>
 * redis.INSTANCE_NAME.maxWaitingHandlers=32<br/>
 * redis.INSTANCE_NAME.maxPoolWaiting=24<br/>
 * redis.INSTANCE_NAME.poolCleanerInterval=5000<br/>
 * </p>
 */
@NullMarked
public class RedisConfig extends ConfigElement {

    /**
     * Constructs a RedisConfig instance from an existing configuration element.
     *
     * @param another The source configuration element to copy from
     * @throws NullPointerException If another is null
     */
    public RedisConfig(ConfigElement another) {
        super(another);
    }

    /**
     * Gets the Redis connection URL.
     * The URL format should be {@code redis://[:password@]host[:port][/db-number]}
     *
     * @return The Redis connection URL
     * @throws NullPointerException If the URL is not configured
     */

    public String getUrl() throws NotConfiguredException {
        return readString(List.of("url"));
    }

    /**
     * Gets the maximum number of connections in the Redis connection pool.
     * Maximum number of concurrent connections maintained in the connection pool.
     * Higher values allow more concurrent operations but consume more resources.
     * For high-concurrency environments, increase this value based on your
     * application needs and Redis server
     * capacity.
     *
     * @return The maximum pool size defaults to 16 if not specified
     */
    public int getMaxPoolSize() {
        try {
            return readInteger(List.of("maxPoolSize"));
        } catch (NotConfiguredException e) {
            return 16;
        }
    }

    /**
     * Gets the maximum number of waiting handlers when the connection pool is full.
     * Maximum number of operation handlers that can wait for a connection when all
     * connections are busy.
     * This prevents unbounded growth of waiting operations when Redis is under
     * a heavy load or unresponsive.
     *
     * @return The maximum number of waiting handlers, defaults to 32 if not
     *         specified
     */
    public int getMaxWaitingHandlers() {
        try {
            return readInteger(List.of("maxWaitingHandlers"));
        } catch (NotConfiguredException e) {
            return 32;
        }
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
        try {
            return readInteger(List.of("maxPoolWaiting"));
        } catch (NotConfiguredException e) {
            return 24;
        }
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
        try {
            return readInteger(List.of("poolCleanerInterval"));
        } catch (NotConfiguredException e) {
            return 5000;
        }
    }

    /**
     * Converts this configuration into a Vert.x RedisOptions object.
     * Applies all configured Redis connection parameters.
     *
     * @return A new RedisOptions object configured with the settings from this
     *         config
     */
    public RedisOptions toRedisOptions() throws NotConfiguredException {
        return new RedisOptions()
                .setConnectionString(getUrl())
                .setMaxPoolSize(getMaxPoolSize())
                .setMaxWaitingHandlers(getMaxWaitingHandlers())
                .setMaxPoolWaiting(getMaxPoolWaiting())
                .setPoolCleanerInterval(getPoolCleanerInterval());
    }
}
