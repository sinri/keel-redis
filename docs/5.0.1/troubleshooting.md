# 故障排查

## `NotConfiguredException`（URL 缺失）

**现象**：构造 `RedisKit` 或调用 `toRedisOptions()` 失败。

**处理**：确认传入 `RedisConfig` 的 `ConfigElement` 根节点下存在 **`url`
** 键，且与 [configuration.md](configuration.md) 中的路径约定一致。

## `getTTLInSecond` / `getTTLInMillisecond` 失败

**现象**：Future 失败，异常信息提示 key 不存在或无过期。

**原因**：封装将 Redis 的 `-1`/`-2` 类响应视为错误，而非成功返回负数。

**处理**：用 `doesKeyExist` 预判，或对 Future 使用 `recover`；不要假设一定返回数值。

## 事务不生效或随机失败

**现象**：MULTI/EXEC 行为异常。

**处理**：确保使用 **`withTransaction`**，不要使用已弃用的 `multi`/`exec`（会直接抛
`UnsupportedOperationException` 或历史上错误用法）。

## WATCH 冲突

**现象**：`withTransaction(watchKeys, fn)` 失败，消息含「事务被打断」类描述。

**原因**：监视的 key 在 EXEC 前被其他客户端修改。

**处理**：业务层重试或合并为 Lua 脚本（本库不内置脚本封装）。

## 连接池耗尽 / 延迟飙升

**可能原因**：

- 大量使用 **阻塞命令**（BLPOP、BZPOPMAX 等），长超时占用连接。
- `maxPoolSize`、`maxWaitingHandlers` 过小。

**处理**：调参、限流、拆分阻塞消费者、或非阻塞 redesign。参见 [redis-kit.md](redis-kit.md)。

## RESP2 / RESP3 与 Hash 响应

**现象**：`getAllHashFields` 等行为与预期略有差异。

**说明**：实现中对 RESP3 Map 与 RESP2 扁平数组两种响应都做了兼容；若升级 Vert.x / Redis 协议，仍应以实测为准。

## 集群与 CROSSSLOT

**现象**：`RENAME`、`MIGRATE`、多 key 事务等报错。

**原因**：Redis Cluster 对多 key 操作有 **hash slot** 限制。

**处理**：保证相关 key 使用相同 **hash tag**（如 `{user}:1`），或改为单 key 操作/脚本。

## 模块路径 / 反射

**现象**：JPMS 下无法访问非导出类型。

**说明**：仅使用 **`exports`** 的包：`kit`、`cache`。Mixin 接口虽为 `public`，若未来收紧导出，请以 `module-info` 为准。

## 仍无法解决

1. 打开 **`io.vertx.redis.client`** 的调试日志，确认命令与响应。
2. 对照 [Vert.x Redis Client 文档](https://vertx.io/docs/vertx-redis-client/java/) 与本仓库 **5.0.1** 标签源码。
3. 在 [GitHub Issues](https://github.com/sinri/keel-redis/issues) 提交最小复现（去掉敏感连接信息）。
