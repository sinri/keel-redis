# keel-redis 项目问题清单

## 一、BUG 级别问题

### 1. `SetMode.EX` 命名错误 — 实际语义是 XX
**文件**: `RedisScalarMixin.java:525-535`
```java
enum SetMode {
    None,
    NX,  // 只有键key不存在的时候才会设置key的值
    EX   // 注释写的是 "只有键key存在的时候才会设置key的值"，这是 XX 的语义
}
```
枚举值 `EX` 的注释描述的是 Redis `XX` 选项的行为，但命名为 `EX`。在 Redis 中 `EX` 是 "expire in seconds" 的含义，与此处语义完全不同。应改名为 `XX`。

**结论**: 已修复。将 `SetMode.EX` 重命名为 `SetMode.XX`，使 `setMode.name()` 正确生成 `"XX"` 参数传递给 Redis SET 命令。（commit `2d83ef7`）

### 2. `seekElementInList` 调用了错误的 Redis 命令
**文件**: `RedisListMixin.java:244`
```java
return api.lpop(args)...  // 应该是 api.lpos(args)
```
`LPOS` 命令用于查找元素位置，但代码中误调用了 `LPOP`（弹出元素）。这会导致**元素被删除**而非查找位置，是严重的数据破坏性 BUG。

**结论**: 已修复。将 `api.lpop(args)` 改为 `api.lpos(args)`，Vert.x RedisAPI 提供的 `lpos` 方法签名与 `lpop` 完全兼容，无需其他改动。

### 3. `getTTLInMillisecond` 和 `getTTLInSecond` 不返回 TTL 值
**文件**: `RedisApiMixin.java:190-218`
```java
default Future<Long> getTTLInMillisecond(String key) {
    return api(api -> api.pttl(key)
        .compose(response -> {
            var ttl = response.toLong();
            if (ttl < 0) {
                return Future.failedFuture(...);
            }
            return Future.succeededFuture();  // ← 丢失了 ttl 值！
        }));
}
```
成功分支返回 `Future.succeededFuture()` 而非 `Future.succeededFuture(ttl)`，调用者永远拿到 `null`。

**结论**: 已修复。将两处 `Future.succeededFuture()` 改为 `Future.succeededFuture(ttl)`，使 TTL 值正确返回给调用者。

### 4. `replaceString` 返回类型不可为 null 但实际可能返回 null
**文件**: `RedisScalarMixin.java:120`
```java
default Future<String> replaceString(String key, String newValue) {
    // 当 key 不存在时返回 Future.succeededFuture() 即 null
```
返回类型声明为 `Future<String>` 而非 `Future<@Nullable String>`，与 `@NullMarked` 注解矛盾。`getString`、`getdel`、`getex`、`getset`、`randomKey`、`objectEncoding` 等多个方法存在同样问题。

**结论**: 已修复。为以下方法的返回类型添加 `@Nullable` 注解：`replaceString`（RedisScalarMixin）、`randomKey`、`objectEncoding`、`getdel`、`getex`、`getset`（RedisApiMixin）。`getString` 已有 `@Nullable` 标注，无需修改。

### 5. `restore` 方法将二进制数据错误转为 String
**文件**: `RedisApiMixin.java:326`
```java
args.add(new String(serializedValue));  // RDB 序列化数据是二进制的，不能这样转换
```
RDB 格式的序列化值是二进制数据，用 `new String(byte[])` 转换会导致数据损坏，RESTORE 命令必然失败。

**结论**: 已修复。绕过 `RedisAPI.restore(List<String>)` 高层封装，改用底层 `RedisConnection.send(Request)` API，通过 `Request.cmd(Command.RESTORE).arg(key).arg(ttl).arg(serializedValue)` 以 `byte[]` 原生传递二进制 RDB 数据。

---

## 二、设计缺陷

### 6. 每次操作都新建连接，未利用连接池
**文件**: `RedisApiMixin.java:36-43`
```java
default <T> Future<T> api(Function<RedisAPI, Future<T>> function) {
    return Future.succeededFuture()
        .compose(v -> this.getClient().connect())  // 每次都新建连接
        .map(RedisAPI::api)
        .compose(api -> Future.succeededFuture()
            .compose(v -> function.apply(api))
            .andThen(ar -> api.close()));           // 用完立即关闭
}
```
Vert.x Redis 客户端本身支持连接池，`RedisConfig` 也配置了 `maxPoolSize`。但 `api()` 方法每次显式 `connect()` + `close()`，绕过了连接池复用机制。高并发场景下连接创建/销毁开销极大。

### 7. 事务 (MULTI/EXEC/WATCH) 实现根本不可用
**文件**: `RedisApiMixin.java:973-1070`

`multi()`、`exec()`、`watch()`、`discard()` 每个方法各自通过 `api()` 获取**独立的连接**。Redis 事务要求 MULTI、命令队列、EXEC 在**同一连接**上执行。当前实现中：
- `multi()` 在连接 A 上执行 MULTI，然后关闭连接 A
- 后续命令在连接 B 上执行（B 并没有处于 MULTI 状态）
- `exec()` 在连接 C 上执行，没有事务上下文

这些事务方法完全无法实现事务语义。

### 8. `clientSetname` / `clientGetname` / `clientId` 无实际用途
同理，由于每次 `api()` 调用使用不同连接，`clientSetname` 设置的名字在连接关闭后立刻失效，`clientId` 返回的 ID 也对应一个已关闭的连接。

### 9. `STRALGO` 方法使用已移除的命令
**文件**: `RedisScalarMixin.java:378-496`

`getLongestCommonSubsequenceUsingStrAlgo` 和 `getLongestCommonSubsequenceWithIdxWithStrAlgo` 使用 `STRALGO` 命令，该命令在 Redis 7.0 中已被移除。虽然提供了 `getLCS` 替代方法，但旧方法未标记 `@Deprecated`。

### 10. `scanHash` 与 `hscan` 功能重复
**文件**: `RedisHashMixin.java:204-231` vs `RedisApiMixin.java:664-698`

`RedisHashMixin.scanHash()` 返回 `Map<String, Object>`（弱类型），而 `RedisApiMixin.hscan()` 返回强类型的 `HScanResult`。两者功能完全相同，前者的返回类型设计更差。

---

## 三、代码质量问题

### 11. `renameKeyIfNewKeyNotExists` 返回值判断有误
**文件**: `RedisApiMixin.java:275-283`

`RENAMENX` 返回的是整数 `1`（成功）或 `0`（newkey 已存在），不是 `"OK"` 字符串。当前实现在 newkey 已存在时会抛出异常（因为返回 `0` 而不是 `"OK"`），而非正常返回失败状态。

### 12. `KeelAsyncCacheWithRedis.read` 中的冗余 null 检查
**文件**: `KeelAsyncCacheWithRedis.java:39-43`
```java
if (Objects.isNull(value)) {
    return Future.failedFuture(new NotCached("Value is null"));
}
String s = Objects.requireNonNull(value);  // 这行永远不会抛出异常
return Future.succeededFuture(s);
```
`Objects.isNull` 已处理 null 情况，之后的 `Objects.requireNonNull` 完全冗余。

### 13. `KeelAsyncCacheWithRedis.read(key, generator, lifeInSeconds)` 吞掉保存失败异常
**文件**: `KeelAsyncCacheWithRedis.java:71-74`
```java
.recover(throwable -> generator.apply(key)
    .compose(v -> save(key, v, lifeInSeconds)
        .recover(saveFailed -> Future.succeededFuture())  // 静默吞掉错误
        .compose(anyway -> Future.succeededFuture(v))));
```
缓存写入失败被静默忽略，没有任何日志记录，排查问题时会很困难。

### 14. `getAllHashFields` 手动解析可能有问题
**文件**: `RedisHashMixin.java:71-86`

Vert.x 的 `HGETALL` 响应在 RESP3 协议下已经是 Map 格式，不需要手动按索引配对。当前的 `i += 2` 解析方式在 RESP3 模式下可能得到错误结果。

### 15. 多处方法参数使用 String 而非枚举
- `blockingMoveElementBetweenLists` 的 `from`/`to` 参数接受 `"LEFT"` / `"RIGHT"` 字符串，应使用枚举
- `getex` 的 `expireOption` 参数接受字符串，应使用枚举
- `clientList` 的 `type` 参数接受字符串，应使用枚举

---

## 四、缺失功能

### 16. 完全没有测试
`src/test/` 目录为空。且 `build.gradle.kts` 中测试 include 规则 `io/github/sinri/keel/base/**/*UnitTest.class` 与本项目包名 `io.github.sinri.keel.integration.redis` 不匹配，即使有测试也不会被执行。

### 17. 缺少 Pub/Sub 支持
Redis 的 Publish/Subscribe 功能未实现，是 Redis 最常用的功能之一。

### 18. 缺少 Stream 数据类型支持
`ValueType` 枚举中声明了 `stream` 类型，但没有任何 Stream 操作的 Mixin。

### 19. 缺少 Lua 脚本 (EVAL/EVALSHA) 支持
分布式锁、原子操作等常见场景需要 Lua 脚本支持。

### 20. 缺少 Pipeline 支持
批量操作没有 Pipeline 机制，每个命令独占一个连接，性能极差。

---

## 五、构建与工程问题

### 21. 遗留的 Maven `target/` 目录未清理
项目已迁移到 Gradle，但 `target/` 目录仍然存在，应添加到 `.gitignore` 或删除。

### 22. `module-info.java` 缺失
项目使用 Java 17，但没有 `module-info.java` 文件。虽然不是必须的，但作为发布到 Maven Central 的库，模块化声明能提供更好的封装。

---

## 六、问题优先级总结

| 优先级 | 编号 | 问题 |
|--------|------|------|
| **P0-紧急** | #2 | `seekElementInList` 调用 LPOP 而非 LPOS，导致数据丢失 |
| **P0-紧急** | #7 | 事务命令 MULTI/EXEC/WATCH 跨连接，完全不可用 |
| **P1-严重** | #1 | `SetMode.EX` 应为 `XX`，导致 SET 命令行为错误 |
| **P1-严重** | #3 | `getTTLInSecond`/`getTTLInMillisecond` 不返回值 |
| **P1-严重** | #5 | `restore` 二进制数据损坏 |
| **P1-严重** | #6 | 未利用连接池，每次操作新建/销毁连接 |
| **P2-中等** | #4 | 多处 @Nullable 标注缺失 |
| **P2-中等** | #8 | CLIENT 命令因连接模型而无意义 |
| **P2-中等** | #9 | STRALGO 已移除命令未标记废弃 |
| **P2-中等** | #10 | scanHash 与 hscan 功能重复 |
| **P2-中等** | #11 | RENAMENX 返回值判断有误 |
| **P2-中等** | #14 | HGETALL 在 RESP3 下解析可能出错 |
| **P2-中等** | #16 | 无测试且测试配置不匹配 |
| **P3-改进** | #12 | 冗余 null 检查 |
| **P3-改进** | #13 | 缓存写入失败静默吞掉 |
| **P3-改进** | #15 | 字符串参数应改为枚举 |
| **P3-改进** | #17-20 | 缺少 Pub/Sub、Stream、Lua、Pipeline |
| **P3-改进** | #21-22 | 工程清理 |
