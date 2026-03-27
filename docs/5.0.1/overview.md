# 概述与范围

## 本库是什么

**Keel Integration for Redis
** 在 [Keel](https://github.com/sinri/keel) 异步栈之上，封装 [Eclipse Vert.x Redis Client](https://vertx.io/docs/vertx-redis-client/java/)，提供：

- 面向 **Vert.x `Future`** 的 Redis 命令封装（通过 `RedisKit` 及若干 Mixin 接口的默认方法）。
- 基于 Redis 字符串类型的 **`KeelAsyncCacheWithRedis`**，实现 `KeelAsyncCacheInterface<String, String>`。

## 5.0.1 版本线要点

- **Java**：17（release 17）。
- **JPMS 模块名**：`io.github.sinri.keel.integration.redis`。
- **导出包**：
    - `io.github.sinri.keel.integration.redis.kit` — 连接配置与 `RedisKit`。
    - `io.github.sinri.keel.integration.redis.cache` — Redis 异步缓存实现。

## 依赖关系（与构建一致）

| 依赖                                             | 作用                           |
|------------------------------------------------|------------------------------|
| `io.github.sinri:keel-core` **5.0.1**（`api`）   | `Keel`、`ConfigElement`、缓存接口等 |
| `io.vertx:vertx-redis-client` **5.0.8**（`api`） | `Redis`、`RedisAPI`、连接池       |

编译期可选：`org.jspecify:jspecify`（空安全注解）。

## 不在本库范围内

- 不替代 Redis 官方命令文档；方法注释中的 Redis 语义以 Redis 版本为准。
- 高层「会话、分布式锁、限流」等模式需自行在 `RedisKit` 之上实现。
- `package-info` 中列举的部分能力（如 Pub/Sub 等）为包级描述愿景；**5.0.1 对外 API 以 `exports` 的类为准**。

## 术语

- **池化 `RedisAPI`**：`RedisKit` 内部对 `Redis.createClient` 创建的客户端使用 `RedisAPI.api(client)`；普通命令通过
  `api(...)` 分发，由客户端管理连接。
- **独占连接**：`withConnection` / `withTransaction` 内 `connect()` 取得的连接，用完关闭并归还池。
