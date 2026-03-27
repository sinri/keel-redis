# 连接与配置（RedisConfig）

## 类与职责

`RedisConfig` 继承 Keel 的 `ConfigElement`，从配置树中读取 Redis 相关键，并转换为 Vert.x 的 `RedisOptions`（
`toRedisOptions()`）。

## 配置键（properties 风格示例）

前缀为 `redis.<实例名>.` 时，常见键如下：

| 键                     | 必填 | 说明                                                   | 未配置时的行为                                                    |
|-----------------------|----|------------------------------------------------------|------------------------------------------------------------|
| `url`                 | 是  | 连接串，格式 `redis://[:password@]host[:port][/db-number]` | `getUrl()` / `toRedisOptions()` 抛 `NotConfiguredException` |
| `maxPoolSize`         | 否  | 连接池最大连接数                                             | 默认 `16`                                                    |
| `maxWaitingHandlers`  | 否  | 池满时最多等待的操作处理器数                                       | 默认 `32`                                                    |
| `maxPoolWaiting`      | 否  | 池满时排队的连接请求上限                                         | 默认 `24`                                                    |
| `poolCleanerInterval` | 否  | 清理空闲连接的间隔（毫秒）                                        | 默认 `5000`                                                  |

## URL 说明

- 密码、端口、库号均按 URI 惯例写在连接串中。
- 集群、哨兵等高级拓扑由 **Vert.x Redis Client** 与连接串格式共同决定；请对照 Vert.x 5.x 官方文档与你运维环境。

## 代码入口

```java
RedisConfig cfg = new RedisConfig(configElement); // configElement 需已包含上述键
var options = cfg.toRedisOptions();
```

`RedisKit` 构造函数内部调用 `redisConfig.toRedisOptions()` 与 `Redis.createClient(keel, options)`。

## 与 Keel 配置体系的衔接

`RedisConfig` 的读取路径（`readString` / `readInteger` 的键列表）相对于你传入的 `ConfigElement` 根节点。实践中通常传入 *
*`redis` 下某一实例子节点** 对应的 `ConfigElement`，使 `url`、`maxPoolSize` 等键位于该节点直接子级（与
`RedisConfig` JavaDoc 中的示例一致）。
