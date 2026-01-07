package io.github.sinri.keel.integration.redis.kit;

import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Redis API 调用的基本 Mixin。
 *
 * @since 5.0.0
 */
@NullMarked
interface RedisApiMixin {

    Redis getClient();

    /**
     * Execute a Redis command through RedisAPI with automatic connection
     * management.
     * The connection will be closed after the command is executed, regardless of
     * success or failure.
     *
     * @param function The function to execute the Redis command, taking RedisAPI as
     *                 parameter
     * @param <T>      The return type of the function executed with the RedisAPI
     *                 instance
     * @return A Future containing the result of the Redis command
     */
    default <T> Future<T> api(Function<RedisAPI, Future<T>> function) {
        return Future.succeededFuture()
                     .compose(v -> this.getClient().connect())
                     .map(RedisAPI::api)
                     .compose(api -> Future.succeededFuture()
                                           .compose(v -> function.apply(api))
                                           .andThen(ar -> api.close()));
    }

    /**
     * EXISTS key [key ...]
     * 从 Redis 3.0.3 起可以一次检查多个 key 是否存在。这种情况下，返回待检查 key 中存在的 key 的个数。检查单个 key 返回 1 或
     * 0 。
     * 注意：如果相同的 key 在参数列表中出现了多次，它会被计算多次。所以，如果somekey存在, EXISTS somekey somekey 命令返回
     * 2。
     */
    default Future<Boolean> doesKeyExist(String key) {
        return api(api -> api.exists(List.of(key))
                             .compose(response -> {
                                 Objects.requireNonNull(response);
                                 return Future.succeededFuture(response.toInteger() == 1);
                             }));
    }

    /**
     * EXISTS key [key ...]
     * 从 Redis 3.0.3 起可以一次检查多个 key 是否存在。这种情况下，返回待检查 key 中存在的 key 的个数。检查单个 key 返回 1 或
     * 0 。
     * 注意：如果相同的 key 在参数列表中出现了多次，它会被计算多次。所以，如果somekey存在, EXISTS somekey somekey 命令返回
     * 2。
     */
    default Future<Integer> countExistedKeys(List<String> keys) {
        return api(api -> api.exists(keys)
                             .compose(response -> {
                                 Objects.requireNonNull(response);
                                 return Future.succeededFuture(response.toInteger());
                             }));
    }

    /**
     * DEL key [key ...]
     * Redis DEL 命令用于删除给定的一个或多个 key 。
     * 不存在的 key 会被忽略。
     *
     * @return 被删除 key 的数量。
     */
    default Future<Integer> deleteKeys(List<String> keys) {
        return api(api -> api.del(keys)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    default Future<Integer> deleteKey(String key) {
        return deleteKeys(List.of(key));
    }

    /**
     * UNLINK key [key ...]
     * Redis UNLINK 命令跟 DEL 命令十分相似：用于删除指定的 key 。就像 DEL 一样，如果 key
     * 不存在，则将其忽略。但是，该命令会执行命令之外的线程中执行实际的内存回收，因此它不是阻塞，而 DEL
     * 是阻塞的。这就是命令名称的来源：UNLINK 命令只是将键与键空间断开连接。实际的删除将稍后异步进行。
     *
     * @return 被删除 key 的数量。
     */
    default Future<Integer> unlinkKeys(List<String> keys) {
        return api(api -> api.unlink(keys)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    default Future<Integer> unlinkKey(String key) {
        return unlinkKeys(List.of(key));
    }

    /**
     * TYPE key
     * 以字符串的形式返回存储在 key 中的值的类型。
     * 可返回的类型是: string, list, set, zset,hash 和 stream。
     */
    default Future<ValueType> getValueTypeOfKey(String key) {
        return api(api -> api.type(key)
                             .compose(response -> {
                                 Objects.requireNonNull(response);
                                 String x = response.toString();
                                 return Future.succeededFuture(ValueType.valueOf(x));
                             }));
    }

    /**
     * RANDOMKEY
     * Redis RANDOMKEY 命令从当前数据库中随机返回一个 key 。
     */
    default Future<String> randomKey() {
        return api(api -> api.randomkey().compose(response -> {
            if (response == null) {
                return Future.succeededFuture(null);
            } else {
                return Future.succeededFuture(response.toString());
            }
        }));
    }

    /**
     * EXPIRE key seconds
     * 设置 key 的过期时间（seconds）。 设置的时间过期后，key 会被自动删除。带有超时时间的 key 通常被称为易失的(volatile)。
     * 超时时间只能使用删除 key 或者覆盖 key 的命令清除，包括 DEL, SET, GETSET 和所有的 *STORE 命令。 对于修改 key
     * 中存储的值，而不是用新值替换旧值的命令，不会修改超时时间。例如，自增 key
     * 中存储的值的 INCR , 向list中新增一个值 LPUSH, 或者修改 hash 域的值 HSET ，这些都不会修改 key 的过期时间。
     * 通过使用 PERSIST 命令把 key 改回持久的(persistent) key，这样 key 的过期时间也可以被清除。
     * key使用 RENAME 改名后，过期时间被转移到新 key 上。
     * 已存在的旧 key 使用 RENAME 改名，那么新 key 会继承所有旧 key 的属性。例如，一个名为 KeyA 的 key 使用命令 RENAME
     * Key_B Key_A 改名，新的 KeyA
     * 会继承包括超时时间在内的所有 Key_B 的属性。
     * 特别注意，使用负值调用 EXPIRE/PEXPIRE 或使用过去的时间调用 EXPIREAT/PEXPIREAT ，那么 key 会被删除 deleted
     * 而不是过期。 (因为, 触发的key event 将是 del,
     * 而不是 expired).
     */
    default Future<Void> expire(String key, int seconds) {
        return api(api -> api.expire(List.of(key, String.valueOf(seconds)))
                             .compose(response -> Future.succeededFuture()));
    }

    /**
     * EXPIREAT key timestamp
     * 详细语义功能说明可以参考 EXPIRE。
     * 使用过去的时间戳将会立即删除该 key。
     *
     * @param unixTimestampInSecond 绝对 Unix 时间戳 (自1970年1月1日以来的秒数)
     */
    default Future<Void> expireAt(String key, int unixTimestampInSecond) {
        return api(api -> api.expireat(List.of(key, String.valueOf(unixTimestampInSecond)))
                             .compose(response -> Future.succeededFuture()));
    }

    /**
     * PEXPIRE key milliseconds
     * PEXPIRE 跟 EXPIRE 基本一样，只是过期时间单位是毫秒。
     */
    default Future<Void> expireInMillisecond(String key, long milliseconds) {
        return api(api -> api.pexpire(List.of(key, String.valueOf(milliseconds)))
                             .compose(response -> Future.succeededFuture()));
    }

    /**
     * PEXPIREAT key milliseconds-timestamp
     * Redis PEXPIREAT 命令用于设置 key 的过期时间，时间的格式是uinx时间戳并精确到毫秒。
     */
    default Future<Void> expireAtInMillisecond(String key, long unixTimestampInMilliseconds) {
        return api(api -> api.pexpireat(List.of(key, String.valueOf(unixTimestampInMilliseconds)))
                             .compose(response -> Future.succeededFuture()));
    }

    /**
     * PTTL key
     * Redis PTTL 命令以毫秒为单位返回 key 的剩余过期时间。
     */
    default Future<Long> getTTLInMillisecond(String key) {
        return api(api -> api.pttl(key)
                             .compose(response -> {
                                 var ttl = response.toLong();
                                 if (ttl < 0) {
                                     // Redis 2.6 之前的版本如果 key 不存在或者 key 没有关联超时时间则返回 -1 。
                                     // Redis 2.8 起：//key 不存在返回 -2 //key 存在但是没有关联超时时间返回 -1
                                     return Future.failedFuture(new RuntimeException("key 不存在或者 key 没有关联超时时间"));
                                 }
                                 return Future.succeededFuture();
                             }));
    }

    /**
     * TTL key
     * Redis TTL 命令以秒为单位返回 key 的剩余过期时间。用户客户端检查 key 还可以存在多久。
     */
    default Future<Long> getTTLInSecond(String key) {
        return api(api -> api.ttl(key)
                             .compose(response -> {
                                 var ttl = response.toLong();
                                 if (ttl < 0) {
                                     // Redis 2.6 之前的版本如果 key 不存在或者 key 没有关联超时时间则返回 -1 。
                                     // Redis 2.8 起：//key 不存在返回 -2 //key 存在但是没有关联超时时间返回 -1
                                     return Future.failedFuture(new RuntimeException("key 不存在或者 key 没有关联超时时间"));
                                 }
                                 return Future.succeededFuture();
                             }));
    }

    /**
     * PERSIST key
     * Redis PERSIST 命令用于删除给定 key 的过期时间，使得 key 永不过期。
     */
    default Future<Void> persist(String key) {
        return api(api -> api.persist(key).compose(response -> Future.succeededFuture()));
    }

    /**
     * KEYS pattern
     * Redis KEYS 命令用于查找所有匹配给定模式 pattern 的 key 。
     * 尽管这个操作的时间复杂度是 O(N)，但是常量时间相当小。
     * 例如，在一个普通笔记本上跑 Redis，扫描 100 万个 key 只要40毫秒。
     * Warning: 生产环境使用 KEYS 命令需要非常小心。在大的数据库上执行命令会影响性能。
     * 这个命令适合用来调试和特殊操作，像改变键空间布局。
     * 不要在你的代码中使用 KEYS 。如果你需要一个寻找键空间中的key子集，考虑使用 SCAN 或 sets。
     * 匹配模式:
     * h?llo 匹配 hello, hallo 和 hxllo
     * h*llo 匹配 hllo 和 heeeello
     * h[ae]llo 匹配 hello and hallo, 不匹配 hillo
     * h[^e]llo 匹配 hallo, hbllo, ... 不匹配 hello
     * h[a-b]llo 匹配 hallo 和 hbllo
     * 使用 \ 转义你想匹配的特殊字符。
     */
    default Future<List<String>> keys(String pattern) {
        return api(api -> api.keys(pattern).compose(response -> {
            List<String> list = new ArrayList<>();
            response.forEach(x -> list.add(x.toString()));
            return Future.succeededFuture(list);
        }));
    }

    /**
     * RENAME key newkey
     * 修改 key 的名字为 newkey 。若key 不存在返回错误。
     * 在集群模式下，key 和newkey 需要在同一个 hash slot。key 和newkey有相同的 hash tag 才能重命名。
     * 如果 newkey 存在则会被覆盖，此种情况隐式执行了 DEL 操作，所以如果要删除的key的值很大会有一定的延时，即使RENAME
     * 本身是常量时间复杂度的操作。
     * 在集群模式下，key 和newkey 需要在同一个 hash slot。key 和newkey有相同的 hash tag 才能重命名。
     */
    default Future<Void> renameKey(String oldKey, String newKey) {
        return api(api -> api.rename(oldKey, newKey).compose(response -> {
            if ("OK".equals(response.toString())) {
                return Future.succeededFuture();
            } else {
                throw new RuntimeException(response.toString());
            }
        }));
    }

    /**
     * RENAMENX key newkey
     * Redis Renamenx 命令用于在新的 key 不存在时修改 key 的名称 。若 key 不存在返回错误。
     * 在集群模式下，key 和newkey 需要在同一个 hash slot。key 和newkey有相同的 hash tag 才能重命名。
     */
    default Future<Void> renameKeyIfNewKeyNotExists(String oldKey, String newKey) {
        return api(api -> api.renamenx(oldKey, newKey).compose(response -> {
            if ("OK".equals(response.toString())) {
                return Future.succeededFuture();
            } else {
                throw new RuntimeException(response.toString());
            }
        }));
    }

    /**
     * TOUCH key [key ...]
     * 修改指定 key 的 最后访问时间。忽略不存在的 key。
     *
     * @return 被更新的 key 个数
     */
    default Future<Integer> touch(List<String> keys) {
        return api(api -> api.touch(keys).compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * DUMP key
     * Redis DUMP 命令用于序列化给定 key ，并返回被序列化的值。
     *
     * @return 序列化后的值，使用 RDB 格式
     */
    default Future<byte[]> dump(String key) {
        return api(api -> api.dump(key)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toBytes());
                             }));
    }

    /**
     * RESTORE key ttl serialized-value [REPLACE] [ABSTTL] [IDLETIME seconds] [FREQ frequency]
     * Redis RESTORE 命令用于反序列化给定的序列化值，并将它和给定的 key 关联。
     *
     * @param key             目标键名
     * @param ttl             过期时间（毫秒），0 表示永不过期
     * @param serializedValue 使用 DUMP 命令序列化的值
     * @param replace         是否替换已有的键
     * @return 成功返回 OK
     */
    default Future<Void> restore(String key, long ttl, byte[] serializedValue, boolean replace) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(ttl));
            args.add(new String(serializedValue));
            if (replace) {
                args.add("REPLACE");
            }

            return api.restore(args)
                      .compose(response -> {
                          if ("OK".equals(response.toString())) {
                              return Future.succeededFuture();
                          } else {
                              return Future.failedFuture(new RuntimeException(response.toString()));
                          }
                      });
        });
    }

    /**
     * MIGRATE host port &lt;key|""&gt; destination-db timeout [COPY] [REPLACE] [AUTH password] [AUTH2 username
     * password] [KEYS key [key ...]]
     * Redis MIGRATE 命令用于将 key 原子性地从当前实例传送到目标实例的指定数据库上。
     *
     * @param host          目标 Redis 主机
     * @param port          目标 Redis 端口
     * @param keys          要迁移的键列表
     * @param destinationDb 目标数据库索引
     * @param timeout       超时时间（毫秒）
     * @param copy          为 true 则不删除源实例上的 key
     * @param replace       为 true 则替换目标实例上已存在的 key
     * @return 成功返回 "OK"
     */
    default Future<Void> migrate(String host, int port, List<String> keys, int destinationDb, int timeout, boolean copy, boolean replace) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(host);
            args.add(String.valueOf(port));
            args.add(""); // 使用 KEYS 选项时为空字符串
            args.add(String.valueOf(destinationDb));
            args.add(String.valueOf(timeout));

            if (copy) {
                args.add("COPY");
            }
            if (replace) {
                args.add("REPLACE");
            }

            args.add("KEYS");
            args.addAll(keys);

            return api.migrate(args)
                      .compose(response -> {
                          if ("OK".equals(response.toString())) {
                              return Future.succeededFuture();
                          } else {
                              return Future.failedFuture(new RuntimeException(response.toString()));
                          }
                      });
        });
    }

    /**
     * MOVE key db
     * Redis MOVE 命令用于将当前数据库的 key 移动到给定的数据库 db 当中。
     *
     * @param key 要移动的键
     * @param db  目标数据库索引
     * @return 移动成功返回 1，否则返回 0
     */
    default Future<Boolean> move(String key, int db) {
        return api(api -> api.move(key, String.valueOf(db))
                             .compose(response -> Future.succeededFuture(response.toInteger() == 1)));
    }

    /**
     * OBJECT subcommand [arguments [arguments ...]]
     * Redis OBJECT 命令用于从内部察看给定 key 的 Redis 对象。
     * <p>
     * 支持的子命令:
     * - REFCOUNT: 返回引用计数
     * - ENCODING: 返回对象编码
     * - IDLETIME: 返回空闲时间（秒）
     * - FREQ: 返回访问频率计数器（仅 LFU 模式）
     */
    default Future<String> objectEncoding(String key) {
        return api(api -> api.object(List.of("ENCODING", key))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    default Future<Long> objectRefcount(String key) {
        return api(api -> api.object(List.of("REFCOUNT", key))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toLong());
                             }));
    }

    default Future<Long> objectIdletime(String key) {
        return api(api -> api.object(List.of("IDLETIME", key))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toLong());
                             }));
    }

    default Future<Long> objectFreq(String key) {
        return api(api -> api.object(List.of("FREQ", key))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toLong());
                             }));
    }

    /**
     * SCAN cursor [MATCH pattern] [COUNT count] [TYPE type]
     * Redis SCAN 命令用于迭代数据库中的键。
     *
     * @param cursor  游标
     * @param pattern 匹配模式
     * @param count   单次迭代返回的元素数量
     * @param type    筛选指定类型的键
     * @return 包含下一个游标和匹配的键列表
     */
    default Future<ScanResult> scan(String cursor, @Nullable String pattern, @Nullable Integer count, @Nullable String type) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(cursor);

            if (pattern != null && !pattern.isEmpty()) {
                args.add("MATCH");
                args.add(pattern);
            }

            if (count != null) {
                args.add("COUNT");
                args.add(count.toString());
            }

            if (type != null && !type.isEmpty()) {
                args.add("TYPE");
                args.add(type);
            }

            return api.scan(args)
                      .compose(response -> {
                          String nextCursor = response.get(0).toString();
                          List<String> keys = new ArrayList<>();
                          response.get(1).forEach(item -> keys.add(item.toString()));
                          return Future.succeededFuture(new ScanResult(nextCursor, keys));
                      });
        });
    }

    /**
     * SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC|DESC] [ALPHA] [STORE destination]
     * Redis SORT 命令用于对列表、集合或有序集合中的元素进行排序。
     */
    default Future<List<String>> sort(
            String key, @Nullable String byPattern, @Nullable Integer offset, @Nullable Integer count,
            @Nullable List<String> getPatterns, boolean desc, boolean alpha, @Nullable String storeDestination
    ) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);

            if (byPattern != null && !byPattern.isEmpty()) {
                args.add("BY");
                args.add(byPattern);
            }

            if (offset != null && count != null) {
                args.add("LIMIT");
                args.add(offset.toString());
                args.add(count.toString());
            }

            if (getPatterns != null && !getPatterns.isEmpty()) {
                for (String pattern : getPatterns) {
                    args.add("GET");
                    args.add(pattern);
                }
            }

            if (desc) {
                args.add("DESC");
            }

            if (alpha) {
                args.add("ALPHA");
            }

            if (storeDestination != null && !storeDestination.isEmpty()) {
                args.add("STORE");
                args.add(storeDestination);
            }

            return api.sort(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * WAIT numreplicas timeout
     * Redis WAIT 命令用于阻塞当前客户端，直到所有先前的写命令都成功传输并且至少在指定数量的从节点中得到确认。
     *
     * @param numReplicas 至少需要同步的从节点数量
     * @param timeout     最大等待时间（毫秒）
     * @return 实际同步的从节点数量
     */
    default Future<Integer> wait(int numReplicas, int timeout) {
        return api(api -> api.wait(String.valueOf(numReplicas), String.valueOf(timeout))
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * COPY source destination [DB destination-db] [REPLACE]
     * Redis COPY 命令用于复制源键的值到目标键。
     *
     * @param source        源键名
     * @param destination   目标键名
     * @param destinationDb 目标数据库索引，默认为当前数据库
     * @param replace       是否替换已有的目标键
     * @return 复制成功返回 true，如果源键不存在则返回 false
     */
    default Future<Boolean> copy(String source, String destination, @Nullable Integer destinationDb, boolean replace) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(source);
            args.add(destination);

            if (destinationDb != null) {
                args.add("DB");
                args.add(destinationDb.toString());
            }

            if (replace) {
                args.add("REPLACE");
            }

            return api.copy(args)
                      .compose(response -> Future.succeededFuture(response.toInteger() == 1));
        });
    }

    /**
     * GETDEL key
     * Redis GETDEL 命令用于获取指定键的值，然后删除该键。
     * 这是原子操作。等效于执行 GET 和 DEL 两个命令，但本命令是原子的。
     *
     * @param key 键名
     * @return 指定键的值，如果键不存在则返回 null
     */
    default Future<String> getdel(String key) {
        return api(api -> api.getdel(key)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * GETEX key [EX seconds|PX milliseconds|EXAT unix-time-seconds|PXAT unix-time-milliseconds|PERSIST]
     * Redis GETEX 命令用于获取指定键的值，并设置该键的过期时间。
     * 这是原子操作。等效于执行 GET 和 EXPIRE 两个命令，但本命令是原子的。
     *
     * @param key          键名
     * @param expireOption 过期选项，可以是 EX（秒）、PX（毫秒）、EXAT（Unix时间戳，秒）、PXAT（Unix时间戳，毫秒）或 PERSIST
     * @param expireValue  过期值，当使用 PERSIST 时可为 null
     * @return 指定键的值，如果键不存在则返回 null
     */
    default Future<String> getex(String key, @Nullable String expireOption, @Nullable Long expireValue) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);

            if (expireOption != null && !expireOption.isEmpty()) {
                args.add(expireOption);
                if (expireValue != null && !"PERSIST".equalsIgnoreCase(expireOption)) {
                    args.add(expireValue.toString());
                }
            }

            return api.getex(args)
                      .compose(response -> {
                          if (response == null) {
                              return Future.succeededFuture(null);
                          }
                          return Future.succeededFuture(response.toString());
                      });
        });
    }

    /**
     * GETSET key value
     * Redis GETSET 命令用于设置指定键的值，并返回该键的旧值。
     * 如果键不存在，则返回 null。
     *
     * @param key   键名
     * @param value 新值
     * @return 指定键的旧值，如果键不存在则返回 null
     */
    @Deprecated(since = "4.1.0")
    default Future<String> getset(String key, String value) {
        return api(api -> api.getset(key, value)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * HSCAN key cursor [MATCH pattern] [COUNT count]
     * Redis HSCAN 命令用于迭代哈希表中的键值对。
     *
     * @param key     哈希表的键名
     * @param cursor  游标
     * @param pattern 匹配模式
     * @param count   单次迭代返回的元素数量
     * @return 包含下一个游标和匹配的字段-值对的列表
     */
    default Future<HScanResult> hscan(String key, String cursor, @Nullable String pattern, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(cursor);

            if (pattern != null && !pattern.isEmpty()) {
                args.add("MATCH");
                args.add(pattern);
            }

            if (count != null) {
                args.add("COUNT");
                args.add(count.toString());
            }

            return api.hscan(args)
                      .compose(response -> {
                          String nextCursor = response.get(0).toString();
                          List<String> items = new ArrayList<>();
                          response.get(1).forEach(item -> items.add(item.toString()));

                          // Convert flat list to field-value pairs
                          final int size = items.size();
                          List<FieldValuePair> pairs = new ArrayList<>();
                          for (int i = 0; i < size; i += 2) {
                              if (i + 1 < size) {
                                  pairs.add(new FieldValuePair(items.get(i), items.get(i + 1)));
                              }
                          }

                          return Future.succeededFuture(new HScanResult(nextCursor, pairs));
                      });
        });
    }

    /**
     * SSCAN key cursor [MATCH pattern] [COUNT count]
     * Redis SSCAN 命令用于迭代集合中的元素。
     *
     * @param key     集合的键名
     * @param cursor  游标
     * @param pattern 匹配模式
     * @param count   单次迭代返回的元素数量
     * @return 包含下一个游标和匹配的元素列表
     */
    default Future<SScanResult> sscan(String key, String cursor, @Nullable String pattern, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(cursor);

            if (pattern != null && !pattern.isEmpty()) {
                args.add("MATCH");
                args.add(pattern);
            }

            if (count != null) {
                args.add("COUNT");
                args.add(count.toString());
            }

            return api.sscan(args)
                      .compose(response -> {
                          String nextCursor = response.get(0).toString();
                          List<String> members = new ArrayList<>();
                          response.get(1).forEach(item -> members.add(item.toString()));
                          return Future.succeededFuture(new SScanResult(nextCursor, members));
                      });
        });
    }

    /**
     * ZSCAN key cursor [MATCH pattern] [COUNT count]
     * Redis ZSCAN 命令用于迭代有序集合中的元素。
     *
     * @param key     有序集合的键名
     * @param cursor  游标
     * @param pattern 匹配模式
     * @param count   单次迭代返回的元素数量
     * @return 包含下一个游标和匹配的成员-分数对的列表
     */
    default Future<ZScanResult> zscan(String key, String cursor, @Nullable String pattern, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(cursor);

            if (pattern != null && !pattern.isEmpty()) {
                args.add("MATCH");
                args.add(pattern);
            }

            if (count != null) {
                args.add("COUNT");
                args.add(count.toString());
            }

            return api.zscan(args)
                      .compose(response -> {
                          String nextCursor = response.get(0).toString();
                          List<String> items = new ArrayList<>();
                          response.get(1).forEach(item -> items.add(item.toString()));

                          // Convert flat list to member-score pairs
                          final int size = items.size();
                          List<MemberScorePair> pairs = new ArrayList<>();
                          for (int i = 0; i < size; i += 2) {
                              if (i + 1 < size) {
                                  pairs.add(new MemberScorePair(items.get(i), Double.parseDouble(items.get(i + 1))));
                              }
                          }

                          return Future.succeededFuture(new ZScanResult(nextCursor, pairs));
                      });
        });
    }

    /**
     * CLIENT ID
     * Redis CLIENT ID 命令返回当前连接的唯一 ID。
     *
     * @return 客户端 ID
     */
    default Future<Long> clientId() {
        return api(api -> api.client(List.of("ID"))
                             .compose(response -> Future.succeededFuture(response.toLong())));
    }

    /**
     * CLIENT INFO
     * Redis CLIENT INFO 命令返回当前客户端连接的相关信息。
     *
     * @return 客户端连接信息
     */
    default Future<String> clientInfo() {
        return api(api -> api.client(List.of("INFO"))
                             .compose(response -> Future.succeededFuture(response.toString())));
    }

    /**
     * CLIENT LIST [TYPE normal|master|replica|pubsub]
     * Redis CLIENT LIST 命令返回所有连接到服务器的客户端信息和统计数据。
     *
     * @param type 客户端类型，可选值为 normal、master、replica、pubsub
     * @return 客户端列表信息
     */
    default Future<String> clientList(@Nullable String type) {
        return api(api -> {
            if (type == null || type.isEmpty()) {
                return api.client(List.of("LIST"))
                          .compose(response -> Future.succeededFuture(response.toString()));
            } else {
                return api.client(List.of("LIST", "TYPE", type))
                          .compose(response -> Future.succeededFuture(response.toString()));
            }
        });
    }

    /**
     * CLIENT SETNAME connection-name
     * Redis CLIENT SETNAME 命令用于为当前连接分配一个名字。
     *
     * @param connectionName 连接名
     * @return 设置成功返回 OK
     */
    default Future<Void> clientSetname(String connectionName) {
        return api(api -> api.client(List.of("SETNAME", connectionName))
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException(response.toString()));
                                 }
                             }));
    }

    /**
     * CLIENT GETNAME
     * Redis CLIENT GETNAME 命令用于获取当前连接的名字。
     *
     * @return 连接名，如果没有设置则返回 null
     */
    default Future<String> clientGetname() {
        return api(api -> api.client(List.of("GETNAME"))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * DBSIZE
     * Redis DBSIZE 命令返回当前数据库的 key 的数量。
     *
     * @return 当前数据库的 key 数量
     */
    default Future<Long> dbsize() {
        return api(api -> api.dbsize()
                             .compose(response -> Future.succeededFuture(response.toLong())));
    }

    /**
     * FLUSHDB [ASYNC]
     * Redis FLUSHDB 命令清空当前数据库中的所有 key。
     *
     * @param async 是否异步执行
     * @return 成功返回 OK
     */
    default Future<Void> flushdb(boolean async) {
        return api(api -> {
            if (async) {
                return api.flushdb(List.of("ASYNC"))
                          .compose(response -> {
                              if ("OK".equals(response.toString())) {
                                  return Future.succeededFuture();
                              } else {
                                  return Future.failedFuture(new RuntimeException(response.toString()));
                              }
                          });
            } else {
                return api.flushdb(List.of())
                          .compose(response -> {
                              if ("OK".equals(response.toString())) {
                                  return Future.succeededFuture();
                              } else {
                                  return Future.failedFuture(new RuntimeException(response.toString()));
                              }
                          });
            }
        });
    }

    /**
     * FLUSHALL [ASYNC]
     * Redis FLUSHALL 命令清空整个 Redis 服务器的数据（删除所有数据库的所有 key）。
     *
     * @param async 是否异步执行
     * @return 成功返回 OK
     */
    default Future<Void> flushall(boolean async) {
        return api(api -> {
            if (async) {
                return api.flushall(List.of("ASYNC"))
                          .compose(response -> {
                              if ("OK".equals(response.toString())) {
                                  return Future.succeededFuture();
                              } else {
                                  return Future.failedFuture(new RuntimeException(response.toString()));
                              }
                          });
            } else {
                return api.flushall(List.of())
                          .compose(response -> {
                              if ("OK".equals(response.toString())) {
                                  return Future.succeededFuture();
                              } else {
                                  return Future.failedFuture(new RuntimeException(response.toString()));
                              }
                          });
            }
        });
    }

    /**
     * SAVE
     * Redis SAVE 命令执行一个同步保存操作，将当前 Redis 实例的所有数据快照以 RDB 文件的形式保存到硬盘。
     *
     * @return 成功返回 OK
     */
    default Future<Void> save() {
        return api(api -> api.save()
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException(response.toString()));
                                 }
                             }));
    }

    /**
     * BGSAVE [SCHEDULE]
     * Redis BGSAVE 命令在后台异步保存当前数据库的数据到磁盘。
     *
     * @param schedule 是否只在没有正在执行的 BGSAVE 时安排一个 BGSAVE 操作
     * @return 成功返回 OK
     */
    default Future<String> bgsave(boolean schedule) {
        return api(api -> {
            if (schedule) {
                return api.bgsave(List.of("SCHEDULE"))
                          .compose(response -> Future.succeededFuture(response.toString()));
            } else {
                return api.bgsave(List.of())
                          .compose(response -> Future.succeededFuture(response.toString()));
            }
        });
    }

    /**
     * MULTI
     * Redis MULTI 命令用于标记一个事务块的开始。
     * 事务块内的命令将在 EXEC 命令被调用时按顺序执行。
     *
     * @return 成功返回 OK
     */
    default Future<Void> multi() {
        return api(api -> api.multi()
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException(response.toString()));
                                 }
                             }));
    }

    /**
     * EXEC
     * Redis EXEC 命令用于执行事务块内的所有命令。
     * 被打断的事务会返回错误。
     *
     * @return 事务块内所有命令的返回值，按命令执行的先后顺序排列
     */
    default Future<List<@Nullable Object>> exec() {
        return api(api -> api.exec()
                             .compose(response -> {
                                 if (response == null) {
                                     // 事务被打断
                                     return Future.failedFuture(new RuntimeException("事务被打断"));
                                 }

                                 List<@Nullable Object> results = new ArrayList<>();
                                 response.forEach(item -> {
                                     if (item == null) {
                                         results.add(null);
                                     } else if (item.type() == io.vertx.redis.client.ResponseType.NUMBER) {
                                         results.add(item.toLong());
                                     } else if (item.type() == io.vertx.redis.client.ResponseType.BULK) {
                                         results.add(item.toString());
                                     } else if (item.type() == io.vertx.redis.client.ResponseType.MULTI) {
                                         List<String> multiResults = new ArrayList<>();
                                         item.forEach(subItem -> multiResults.add(subItem.toString()));
                                         results.add(multiResults);
                                     } else {
                                         results.add(item.toString());
                                     }
                                 });

                                 return Future.succeededFuture(results);
                             }));
    }

    /**
     * DISCARD
     * Redis DISCARD 命令用于取消事务，放弃执行事务块内的所有命令。
     *
     * @return 成功返回 OK
     */
    default Future<Void> discard() {
        return api(api -> api.discard()
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException(response.toString()));
                                 }
                             }));
    }

    /**
     * WATCH key [key ...]
     * Redis WATCH 命令用于监视一个或多个 key，如果在事务执行之前这些 key 被其他命令所改动，那么事务将被打断。
     *
     * @param keys 要监视的键
     * @return 成功返回 OK
     */
    default Future<Void> watch(List<String> keys) {
        return api(api -> api.watch(keys)
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException(response.toString()));
                                 }
                             }));
    }

    /**
     * UNWATCH
     * Redis UNWATCH 命令用于取消 WATCH 命令对所有 key 的监视。
     *
     * @return 成功返回 OK
     */
    default Future<Void> unwatch() {
        return api(api -> api.unwatch()
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException(response.toString()));
                                 }
                             }));
    }

    enum ValueType {
        string, list, set, zset, hash, stream, none
    }

    class ScanResult {
        private final String cursor;
        private final List<String> keys;

        public ScanResult(String cursor, List<String> keys) {
            this.cursor = cursor;
            this.keys = keys;
        }

        public String getCursor() {
            return cursor;
        }

        public List<String> getKeys() {
            return keys;
        }
    }

    class HScanResult {
        private final String cursor;
        private final List<FieldValuePair> fieldValuePairs;

        public HScanResult(String cursor, List<FieldValuePair> fieldValuePairs) {
            this.cursor = cursor;
            this.fieldValuePairs = fieldValuePairs;
        }

        public String getCursor() {
            return cursor;
        }

        public List<FieldValuePair> getFieldValuePairs() {
            return fieldValuePairs;
        }
    }

    class FieldValuePair {
        private final String field;
        private final String value;

        public FieldValuePair(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public String getValue() {
            return value;
        }
    }

    class SScanResult {
        private final String cursor;
        private final List<String> members;

        public SScanResult(String cursor, List<String> members) {
            this.cursor = cursor;
            this.members = members;
        }

        public String getCursor() {
            return cursor;
        }

        public List<String> getMembers() {
            return members;
        }
    }

    class ZScanResult {
        private final String cursor;
        private final List<MemberScorePair> memberScorePairs;

        public ZScanResult(String cursor, List<MemberScorePair> memberScorePairs) {
            this.cursor = cursor;
            this.memberScorePairs = memberScorePairs;
        }

        public String getCursor() {
            return cursor;
        }

        public List<MemberScorePair> getMemberScorePairs() {
            return memberScorePairs;
        }
    }

    class MemberScorePair {
        private final String member;
        private final double score;

        public MemberScorePair(String member, double score) {
            this.member = member;
            this.score = score;
        }

        public String getMember() {
            return member;
        }

        public double getScore() {
            return score;
        }
    }
}
