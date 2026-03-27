# 弃用与迁移（5.0.1）

以下为当前源码中标记 **`@Deprecated`** 的 API 及建议替代方式。若未列出 `since`，以 JavaDoc/`@Deprecated(since=...)` 为准。

## 池化连接语义不成立 — 改用 `withConnection` / `withTransaction`

| API                                | 原因摘要             | 替代                                       |
|------------------------------------|------------------|------------------------------------------|
| `clientId()`                       | 池化下连接立即归还，ID 无意义 | `withConnection(api -> api.client(...))` |
| `clientSetname` / `clientGetname`  | 连接级状态在池化下不可靠     | 同上，独占连接执行                                |
| `multi()` / `exec()` / `discard()` | MULTI/EXEC 可能跨连接 | `withTransaction(...)`                   |
| `watch` / `unwatch`                | WATCH 与事务需同连接    | `withTransaction(watchKeys, ...)`        |

## Redis 版本演进

| API                                             | since | 说明                                                   |
|-------------------------------------------------|-------|------------------------------------------------------|
| `getLongestCommonSubsequenceUsingStrAlgo`       | 5.0.0 | `STRALGO` 在 Redis 7.0 移除；用 `getLCS` / `getLCSLength` |
| `getLongestCommonSubsequenceWithIdxWithStrAlgo` | 5.0.0 | 同上                                                   |

## 命令替代（更好或更一致的封装）

| API                                   | since | 替代                                                              |
|---------------------------------------|-------|-----------------------------------------------------------------|
| `getset`（`RedisApiMixin`）             | 4.1.0 | 使用带 GET 选项的 `SET`（Vert.x 原生）或业务层组合                              |
| `replaceString`（`RedisScalarMixin`）   | 4.1.0 | 同上（GETSET）                                                      |
| `popTailAndPushHead`                  | 5.0.0 | `moveElementBetweenLists`                                       |
| `blockingPopTailAndPushHead`          | 5.0.0 | `blockingMoveElementBetweenLists`                               |
| `setMultipleHashFields`（HMSET）        | 4.1.0 | `setHashFields`（HSET 多域）                                        |
| `scanHash`                            | 5.0.0 | `hscan`（强类型 `HScanResult`）                                      |
| 有序集合若干 `*RangeBy*` / `*ReverseRange*` | 4.1.0 | Redis 6.2+ 推荐使用 `ZRANGE` 等新选项；请查阅 Redis 官方迁移指南并在 Vert.x API 层组合 |

## 升级检查清单

1. 全文搜索 `multi(`、`exec(`、`watch(`，改为 `withTransaction`。
2. 搜索 `clientSetname`、`clientGetname`、`clientId`，改为 `withConnection`。
3. 移除对 `STRALGO` 封装方法的依赖（Redis 7）。
4. 列表 `RPOPLPUSH` / `BRPOPLPUSH` 封装迁移到 `LMOVE` / `BLMOVE` 风格方法。
