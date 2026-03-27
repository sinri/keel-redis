# RedisKit 使用指南

## 核心类型

- **`RedisKit`**：持有 `Redis` 客户端与池化 `RedisAPI`，并实现多个 Mixin 接口，对外表现为**一个** Redis 工具入口。
- **Mixin 接口**（包内 `interface`，由 `RedisKit` 组合）：`RedisApiMixin`、`RedisScalarMixin`、`RedisListMixin`、
  `RedisHashMixin`、`RedisSetMixin`、`RedisOrderedSetMixin`、`RedisBitMixin`。

所有命令风格方法返回 **`io.vertx.core.Future`**，在 Vert.x/Keel 异步模型中使用。

## 三种执行方式

### 1. `api(Function<RedisAPI, Future<T>>)`（默认）

大多数封装方法内部调用 `api(...)`，使用**池化** `RedisAPI`。适合绝大多数独立命令。

### 2. `withConnection(Function<RedisAPI, Future<T>>)`

在**独占连接**上执行逻辑，结束后关闭连接并归还池。

**适用**：依赖「同一连接」的语义，例如：

- `CLIENT SETNAME` / `CLIENT GETNAME` / `CLIENT ID`（见 [deprecations.md](deprecations.md) 中已弃用的池化封装）。
- 其他必须在单连接上完成的交互。

### 3. `withTransaction` / `withTransaction(watchKeys, function)`

在**同一连接**上执行 **MULTI/EXEC** 事务：

- `withTransaction(f)`：无 WATCH。
- `withTransaction(watchKeys, f)`：先 WATCH 再 MULTI；若 EXEC 因 WATCH 冲突返回 null，Future 失败并带说明性异常。

用户函数失败时会尝试 **DISCARD**。

**重要**：不要使用已弃用且会抛异常的 `multi()` / `exec()` / `discard()` / `watch()` /
`unwatch()` 默认方法（它们在池化模式下无法保证同一连接）。详见 [deprecations.md](deprecations.md)。

## 关闭与资源

- 使用完毕后调用 **`RedisKit.close()`**（返回 `Future<Void>`），或 Vert.x `Closeable` 的 `close(Completable)` 重载。
- 正常运行期间**不要**关闭 `getRedisAPI()`，否则与连接池管理冲突。

## 阻塞类命令与连接池

以下 API 在文档或源码中明确提示：**阻塞期间占用池内连接**，高并发或长超时可能**耗尽连接池**：

- 列表：`blockingMoveElementBetweenLists`、`blockingPopFromListsHead`、`blockingPopFromListsTail`、
  `blockingPopTailAndPushHead`（已弃用）等。
- 有序集合：`blockingPopMaxFromOrderedSets`、`blockingPopMinFromOrderedSets`。

建议：

- 控制并发度、缩短超时，或为阻塞消费单独部署实例/独立客户端。
- 评估是否改用非阻塞原语或消息队列。

## TTL 相关失败语义

`getTTLInSecond`、`getTTLInMillisecond` 在 Redis 返回负数（key 不存在或无过期）时**失败**（`Future` failed），而不是返回负数。调用方需
`recover` 或先 `doesKeyExist` 等，按业务选择。

## 扩展命令

若封装未覆盖某条 Redis 命令，可：

- 使用 `getRedisAPI()` 在 `api(...)` 中直接调用 Vert.x `RedisAPI` 的对应方法；或
- 使用 `withConnection` 发送底层 `Request`（参考 `RedisApiMixin.restore` 使用 `getClient().connect()` 与 `Command` 的写法）。
