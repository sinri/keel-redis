# 快速开始

下列示例展示**概念步骤**：你仍需按 Keel 项目的惯例取得 `Keel` 实例、加载配置并得到对应路径下的 `ConfigElement`（例如
`redis.<实例名>` 节点）。具体配置加载方式请参考 **keel-core** 文档。

## 1. 准备配置

在属性配置中提供 Redis 实例块（键名中的 `INSTANCE_NAME` 替换为你的实例标识）：

```properties
redis.INSTANCE_NAME.url=redis://:password@127.0.0.1:6379/0
redis.INSTANCE_NAME.maxPoolSize=16
redis.INSTANCE_NAME.maxWaitingHandlers=32
redis.INSTANCE_NAME.maxPoolWaiting=24
redis.INSTANCE_NAME.poolCleanerInterval=5000
```

除 `url` 外均可省略，库内对数值项有默认值（见 [configuration.md](configuration.md)）。

## 2. 构造 RedisKit

```java
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.integration.redis.kit.RedisConfig;
import io.github.sinri.keel.integration.redis.kit.RedisKit;

// 假设 configElement 已指向 redis.INSTANCE_NAME 对应的 ConfigElement
RedisConfig redisConfig = new RedisConfig(configElement);
RedisKit redis = new RedisKit(keel, redisConfig);
```

## 3. 执行命令

```java
redis.getString("my-key")
    .onSuccess(v -> { /* ... */ })
    .onFailure(err -> { /* ... */ });
```

## 4. 关闭

应用停止时应关闭客户端，释放连接池：

```java
redis.close()
    .onComplete(ar -> { /* 记录日志或继续关闭 Vert.x */ });
```

说明：

- `RedisKit` 实现 `io.vertx.core.Closeable`，也可通过 Vert.x 生命周期统一管理。
- **不要**在日常业务中调用 `redis.getRedisAPI().close()`；仅在整体关闭流程中由 `RedisKit.close()` 处理（见源码注释）。

## 5. 可选：缓存门面

```java
import io.github.sinri.keel.integration.redis.cache.KeelAsyncCacheWithRedis;

var cache = new KeelAsyncCacheWithRedis(redis);
cache.save("k", "v", 120)
     .compose(v -> cache.read("k"));
```

详见 [async-cache.md](async-cache.md)。
