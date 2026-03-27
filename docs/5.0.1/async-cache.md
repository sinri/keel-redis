# 异步缓存（KeelAsyncCacheWithRedis）

## 类型

`KeelAsyncCacheWithRedis` 实现 **`KeelAsyncCacheInterface<String, String>`**（定义在 `keel-core`），用 Redis **字符串
**键值存储缓存条目。

## 构造

```java
var cache = new KeelAsyncCacheWithRedis(redisKit);
```

## 行为摘要

| 方法                                    | 行为                                                                                 |
|---------------------------------------|------------------------------------------------------------------------------------|
| `save(key, value, lifeInSeconds)`     | `SET` 并带过期（秒）；内部将 `lifeInSeconds` 转为 `int`，极大值需注意溢出                                |
| `save(key, value)`                    | `value == null` 时等价于 `remove(key)`；否则使用 **默认 TTL `DEFAULT_LIFE_IN_SECONDS`（60 秒）** |
| `read(key)`                           | `GET`；值为 null 时 **失败**（`NotCached`）                                                |
| `read(key, fallbackValue)`            | 失败时返回 `fallbackValue`                                                              |
| `read(key, generator, lifeInSeconds)` | 缓存未命中时调用 `generator`，成功后写入并返回；写入失败仅打日志，仍返回生成值                                      |
| `remove(key)`                         | 删除键                                                                                |
| `removeAll()`                         | **不支持**（`UnsupportedOperationException`）                                           |
| `cleanUp()`                           | 立即成功（依赖 Redis 自身过期）                                                                |
| `getCachedKeySet()`                   | **不支持**（避免生产滥用 `KEYS` 等）                                                           |

## 选型建议

- 适合：**字符串缓存**、与 Keel 缓存接口对齐、TTL 以秒为主。
- 若需 **Hash / JSON 序列化 / 命名空间批量清理**，请在 `RedisKit` 上直接建模，或另写适配层。

## 与 RedisKit 的关系

缓存实现**不**接管 `RedisKit` 生命周期；仍须在应用停止时关闭 `RedisKit`。
