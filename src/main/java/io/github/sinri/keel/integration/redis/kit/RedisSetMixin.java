package io.github.sinri.keel.integration.redis.kit;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis API 调用中 Set 相关的 Mixin。
 *
 * @since 5.0.0
 */
@NullMarked
interface RedisSetMixin extends RedisApiMixin {
    /**
     * SADD key member [member ...]
     * Redis SADD 命令将一个或多个成员元素加入到集合中，已经存在于集合的成员元素将被忽略。
     * 假如集合 key 不存在，则创建一个只包含添加的元素作成员的集合。
     * 当集合 key 不是集合类型时，返回一个错误。
     *
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
     */
    default Future<Integer> addToSet(String key, List<String> members) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(members);
            return api.sadd(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    default Future<Integer> addToSet(String key, String member) {
        return addToSet(key, List.of(member));
    }

    /**
     * SCARD key
     * Redis SCARD 命令返回集合中元素的数量。
     * 当集合 key 不存在时，返回 0。
     *
     * @return 集合的基数(集合中元素的数量)。
     */
    default Future<Integer> getSetCardinality(String key) {
        return api(api -> api.scard(key).compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * SDIFF key [key ...]
     * Redis SDIFF 命令返回第一个集合与其他集合之间的差集。
     * 不存在的集合 key 被视为空集。
     *
     * @return 包含差集成员的列表。
     */
    default Future<Set<String>> getSetDifference(List<String> keys) {
        return api(api -> api.sdiff(keys).compose(response -> {
            Set<String> result = new HashSet<>();
            if (response != null) {
                response.forEach(item -> result.add(item.toString()));
            }
            return Future.succeededFuture(result);
        }));
    }

    /**
     * SDIFFSTORE destination key [key ...]
     * Redis SDIFFSTORE 命令将给定集合之间的差集存储在指定的集合中。
     * 如果指定的集合 destination 已存在，则会被覆盖。
     *
     * @return 结果集中的元素数量。
     */
    default Future<Integer> storeSetDifference(String destination, List<String> keys) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(destination);
            args.addAll(keys);
            return api.sdiffstore(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * SINTER key [key ...]
     * Redis SINTER 命令返回给定所有集合的交集。
     * 不存在的集合 key 被视为空集。
     * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。
     *
     * @return 交集成员的列表。
     */
    default Future<Set<String>> getSetIntersection(List<String> keys) {
        return api(api -> api.sinter(keys).compose(response -> {
            Set<String> result = new HashSet<>();
            if (response != null) {
                response.forEach(item -> result.add(item.toString()));
            }
            return Future.succeededFuture(result);
        }));
    }

    /**
     * SINTERSTORE destination key [key ...]
     * Redis SINTERSTORE 命令将给定集合之间的交集存储在指定的集合中。
     * 如果指定的集合 destination 已存在，则会被覆盖。
     *
     * @return 结果集中的元素数量。
     */
    default Future<Integer> storeSetIntersection(String destination, List<String> keys) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(destination);
            args.addAll(keys);
            return api.sinterstore(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * SISMEMBER key member
     * Redis SISMEMBER 命令判断成员元素是否是集合的成员。
     *
     * @return 如果成员元素是集合的成员，返回 true；如果成员元素不是集合的成员，或 key 不存在，返回 false。
     */
    default Future<Boolean> isSetMember(String key, String member) {
        return api(api -> api.sismember(key, member)
                             .compose(response -> Future.succeededFuture(response.toInteger() == 1)));
    }

    /**
     * SMEMBERS key
     * Redis SMEMBERS 命令返回集合中的所有成员。
     * 不存在的集合 key 视为空集合。
     *
     * @return 集合中的所有成员。
     */
    default Future<Set<String>> getSetMembers(String key) {
        return api(api -> api.smembers(key).compose(response -> {
            Set<String> result = new HashSet<>();
            if (response != null) {
                response.forEach(item -> result.add(item.toString()));
            }
            return Future.succeededFuture(result);
        }));
    }

    /**
     * SMISMEMBER key member [member ...]
     * Redis SMISMEMBER 命令判断成员元素是否是集合的成员。
     * 与 SISMEMBER 不同，SMISMEMBER 可以一次检查多个元素。
     *
     * @return 列表，包含与每个成员元素对应的布尔值（0或1），表示该元素是否在集合中。
     */
    default Future<List<Boolean>> areSetMembers(String key, List<String> members) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(members);
            return api.smismember(args).compose(response -> {
                List<Boolean> result = new ArrayList<>();
                if (response != null) {
                    response.forEach(item -> result.add(item.toInteger() == 1));
                }
                return Future.succeededFuture(result);
            });
        });
    }

    /**
     * SMOVE source destination member
     * Redis SMOVE 命令将指定成员 member 元素从 source 集合移动到 destination 集合。
     * SMOVE 是原子性操作。
     * 如果 source 集合不存在或不包含指定的成员元素，则 SMOVE 命令不执行任何操作，返回 0。
     * 否则，成员元素是从 source 集合中移除的，并添加到 destination 集合中。
     * 如果 destination 集合已经包含成员元素，则只在 source 集合中删除该元素。
     *
     * @return 如果成员元素被成功移除，返回 true；如果成员元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，返回 false。
     */
    default Future<Boolean> moveSetMember(String source, String destination, String member) {
        return api(api -> api.smove(source, destination, member)
                             .compose(response -> Future.succeededFuture(response.toInteger() == 1)));
    }

    /**
     * SPOP key [count]
     * Redis SPOP 命令用于移除并返回集合中的一个或多个随机元素。
     * count 参数决定了移除元素的数量。如果 count 大于集合中的元素数量，则返回整个集合。
     *
     * @return 被移除的随机元素。当集合不存在或是空集时，返回 null。
     */
    default Future<List<String>> popRandomSetMembers(String key, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            if (count != null) {
                args.add(count.toString());
            }
            return api.spop(args).compose(response -> {
                List<String> result = new ArrayList<>();
                if (response != null) {
                    if (count == null || count == 1) {
                        result.add(response.toString());
                    } else {
                        response.forEach(item -> result.add(item.toString()));
                    }
                }
                return Future.succeededFuture(result);
            });
        });
    }

    default Future<String> popRandomSetMember(String key) {
        return popRandomSetMembers(key, 1).compose(members -> {
            if (members.isEmpty()) {
                return Future.succeededFuture(null);
            }
            return Future.succeededFuture(members.get(0));
        });
    }

    /**
     * SRANDMEMBER key [count]
     * Redis SRANDMEMBER 命令用于返回集合中的一个或多个随机元素。
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。
     * 如果 count 大于等于集合基数，那么返回整个集合。
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，数组的长度为 count 的绝对值。
     * 与 SPOP 不同，SRANDMEMBER 不会移除被选择的元素。
     *
     * @return 随机元素，或包含随机元素的列表。当集合不存在或是空集时，返回 null。
     */
    default Future<@Nullable List<String>> getRandomSetMembers(String key, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            if (count != null) {
                args.add(count.toString());
            }
            return api.srandmember(args)
                      .compose(response -> {
                          if (response != null) {
                              List<String> result = new ArrayList<>();
                              if (count == null) {
                                  result.add(response.toString());
                              } else {
                                  response.forEach(item -> result.add(item.toString()));
                              }
                              return Future.succeededFuture(result);
                          } else {
                              return Future.succeededFuture(null);
                          }
                      });
        });
    }

    default Future<String> getRandomSetMember(String key) {
        return getRandomSetMembers(key, null)
                .compose(members -> {
                    if (members == null || members.isEmpty()) {
                        return Future.succeededFuture(null);
                    }
                    return Future.succeededFuture(members.get(0));
                });
    }

    /**
     * SREM key member [member ...]
     * Redis SREM 命令用于移除集合中的一个或多个成员元素，不存在的成员元素会被忽略。
     * 当 key 不是集合类型，返回一个错误。
     *
     * @return 被成功移除的元素的数量，不包括被忽略的元素。
     */
    default Future<Integer> removeFromSet(String key, List<String> members) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(members);
            return api.srem(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    default Future<Integer> removeFromSet(String key, String member) {
        return removeFromSet(key, List.of(member));
    }

    /**
     * SSCAN key cursor [MATCH pattern] [COUNT count]
     * Redis SSCAN 命令用于迭代集合中的元素。
     *
     * @param key     集合键
     * @param cursor  游标
     * @param pattern 匹配的模式
     * @param count   指定从数据集里返回多少元素，默认值为 10
     * @return 包含两个元素的Future-Map，第一个是用于下一次迭代的新游标，第二个是匹配的元素列表
     */
    default Future<SScanResult> scanSet(String key, String cursor, @Nullable String pattern, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(cursor);
            if (pattern != null) {
                args.add("MATCH");
                args.add(pattern);
            }
            if (count != null) {
                args.add("COUNT");
                args.add(count.toString());
            }
            return api.sscan(args).compose(response -> {
                String newCursor = response.get(0).toString();
                List<String> elements = new ArrayList<>();
                if (response.get(1) != null) {
                    response.get(1).forEach(item -> elements.add(item.toString()));
                }
                return Future.succeededFuture(new SScanResult(newCursor, elements));
            });
        });
    }

    /**
     * SUNION key [key ...]
     * Redis SUNION 命令返回给定集合的并集。
     * 不存在的集合 key 被视为空集。
     *
     * @return 并集成员的列表。
     */
    default Future<Set<String>> getSetUnion(List<String> keys) {
        return api(api -> api.sunion(keys).compose(response -> {
            Set<String> result = new HashSet<>();
            if (response != null) {
                response.forEach(item -> result.add(item.toString()));
            }
            return Future.succeededFuture(result);
        }));
    }

    /**
     * SUNIONSTORE destination key [key ...]
     * Redis SUNIONSTORE 命令将给定集合的并集存储在指定的集合 destination 中。
     * 如果 destination 已经存在，则将其覆盖。
     *
     * @return 结果集中的元素数量。
     */
    default Future<Integer> storeSetUnion(String destination, List<String> keys) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(destination);
            args.addAll(keys);
            return api.sunionstore(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }
}
