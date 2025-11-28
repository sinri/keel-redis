module io.github.sinri.keel.integration.redis {
    requires io.github.sinri.keel.base;
    requires io.github.sinri.keel.core;
    requires io.vertx.core;
    requires transitive io.vertx.redis.client;
    requires static org.jetbrains.annotations;

    exports io.github.sinri.keel.integration.redis.kit;
    exports io.github.sinri.keel.integration.redis.cache;
}