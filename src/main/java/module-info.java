module io.github.sinri.keel.integration.redis {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.vertx.core;
    requires transitive io.vertx.redis.client;
    requires static org.jetbrains.annotations;

    exports io.github.sinri.keel.integration.redis.kit;
    exports io.github.sinri.keel.integration.redis.cache;
}