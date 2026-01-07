package io.github.sinri.keel.integration.redis.kit;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis API 调用中 Ordered Set 相关的 Mixin。
 *
 * @since 5.0.0
 */
@NullMarked
interface RedisOrderedSetMixin extends RedisApiMixin {

    /**
     * ZADD key [NX|XX] [GT|LT] [CH] [INCR] score member [score member ...]
     * Redis ZADD 命令将一个或多个成员元素及其分数值加入到有序集当中。
     * 如果某个成员已经是有序集的成员，那么更新这个成员的分数值，并通过重新插入这个成员元素，来保证该成员在正确的位置上。
     * 分数值可以是整数值或双精度浮点数。
     * 如果有序集合 key 不存在，则创建一个空的有序集并执行 ZADD 操作。
     * 当 key 存在但不是有序集类型时，返回一个错误。
     *
     * @param key          有序集合键名
     * @param options      可选参数映射，可包含以下选项：
     *                     "NX"  - 仅添加新成员，不更新已存在的成员
     *                     "XX"  - 仅更新已存在的成员，不添加新成员
     *                     "GT"  - 仅当新分数大于当前分数时才更新
     *                     "LT"  - 仅当新分数小于当前分数时才更新
     *                     "CH"  - 修改返回值为发生变化的成员总数
     *                     "INCR" - 以增量方式增加分数（类似ZINCRBY）
     * @param memberScores 成员-分数映射
     * @return 被成功添加或更新的成员数量（不计算被忽略的成员）
     */
    default Future<Integer> addToOrderedSet(String key, @Nullable Map<String, @Nullable Object> options, Map<String, Double> memberScores) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);

            // 添加选项
            if (options != null) {
                for (Map.Entry<String, @Nullable Object> entry : options.entrySet()) {
                    if (entry.getValue() != null) {
                        args.add(entry.getKey());
                    }
                }
            }

            // 添加成员和分数
            for (Map.Entry<String, Double> entry : memberScores.entrySet()) {
                args.add(String.valueOf(entry.getValue()));
                args.add(entry.getKey());
            }

            return api.zadd(args)
                      .compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * 简化版的ZADD命令，无选项参数
     */
    default Future<Integer> addToOrderedSet(String key, Map<String, Double> memberScores) {
        return addToOrderedSet(key, null, memberScores);
    }

    /**
     * 单个成员添加的简化版
     */
    default Future<Integer> addToOrderedSet(String key, String member, double score) {
        Map<String, Double> memberScores = new HashMap<>();
        memberScores.put(member, score);
        return addToOrderedSet(key, null, memberScores);
    }

    /**
     * ZCARD key
     * Redis ZCARD 命令用于获取有序集合中的成员数量。
     *
     * @param key 有序集合键名
     * @return 有序集合的成员数量，当键不存在时返回0
     */
    default Future<Integer> getOrderedSetSize(String key) {
        return api(api -> api.zcard(key)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * ZCOUNT key min max
     * Redis ZCOUNT 命令用于获取有序集合中指定分数区间的成员数量。
     * 分数区间可以使用排除边界的符号 "(" 或包含边界的符号 "["，或者使用特殊值 "-inf" 和 "+inf" 表示无限小和无限大。
     *
     * @param key 有序集合键名
     * @param min 最小分数（包含），使用"(score"表示开区间
     * @param max 最大分数（包含），使用"(score"表示开区间
     * @return 指定分数区间的成员数量
     */
    default Future<Integer> countOrderedSetElementsInScoreRange(String key, String min, String max) {
        return api(api -> api.zcount(key, min, max)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * ZINCRBY key increment member
     * Redis ZINCRBY 命令对有序集合中指定成员的分数加上增量 increment。
     * 如果成员不存在，则添加成员并设置分数为 increment。
     * increment 可以是负数，会相应减少分数。
     *
     * @param key       有序集合键名
     * @param increment 增量值（可为负数）
     * @param member    成员名称
     * @return 增加后的分数
     */
    default Future<Double> incrementOrderedSetMemberScore(String key, double increment, String member) {
        return api(api -> api.zincrby(key, String.valueOf(increment), member)
                             .compose(response -> Future.succeededFuture(Double.parseDouble(response.toString()))));
    }

    /**
     * ZINTER numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
     * Redis ZINTER 命令用于计算给定的有序集的交集，并将结果与分数一起返回。
     * 不存在的有序集合被认为是空的，因此如果有输入的键不存在，结果也为空。
     *
     * @param keys       要计算交集的有序集合键名列表
     * @param weights    可选的权重列表，与输入键顺序对应
     * @param aggregate  可选的聚合方式（SUM、MIN、MAX）
     * @param withScores 是否返回分数
     * @return 计算结果成员列表，如果withScores为true，则为成员和分数交替的列表
     */
    default Future<List<String>> intersectOrderedSets(List<String> keys, @Nullable List<Double> weights, @Nullable String aggregate, boolean withScores) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(keys.size()));
            args.addAll(keys);

            if (weights != null && !weights.isEmpty()) {
                args.add("WEIGHTS");
                for (Double weight : weights) {
                    args.add(String.valueOf(weight));
                }
            }

            if (aggregate != null && !aggregate.isEmpty()) {
                args.add("AGGREGATE");
                args.add(aggregate);
            }

            if (withScores) {
                args.add("WITHSCORES");
            }

            return api.zinter(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
     * Redis ZINTERSTORE 命令计算给定的有序集的交集，并将结果存储到新的有序集合 destination 中。
     *
     * @param destination 目标有序集合键名
     * @param keys        要计算交集的有序集合键名列表
     * @param weights     可选的权重列表，与输入键顺序对应
     * @param aggregate   可选的聚合方式（SUM、MIN、MAX）
     * @return 目标有序集合中的元素数量
     */
    default Future<Integer> storeIntersectionOfOrderedSets(String destination, List<String> keys, @Nullable List<Double> weights, @Nullable String aggregate) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(destination);
            args.add(String.valueOf(keys.size()));
            args.addAll(keys);

            if (weights != null && !weights.isEmpty()) {
                args.add("WEIGHTS");
                for (Double weight : weights) {
                    args.add(String.valueOf(weight));
                }
            }

            if (aggregate != null && !aggregate.isEmpty()) {
                args.add("AGGREGATE");
                args.add(aggregate);
            }

            return api.zinterstore(args)
                      .compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * ZLEXCOUNT key min max
     * Redis ZLEXCOUNT 命令用于计算有序集合中指定字典序区间内成员数量。
     * 有序集合中的所有成员必须具有相同的分数，否则排序就不是按字典序了。
     *
     * @param key 有序集合键名
     * @param min 字典序范围的最小值，"[member"表示闭区间，"(member"表示开区间，"-"表示无限小
     * @param max 字典序范围的最大值，"[member"表示闭区间，"(member"表示开区间，"+"表示无限大
     * @return 指定区间内的成员数量
     */
    default Future<Integer> countOrderedSetElementsInLexRange(String key, String min, String max) {
        return api(api -> api.zlexcount(key, min, max)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * ZMSCORE key member [member ...]
     * Redis ZMSCORE 命令返回有序集中一个或多个成员的分数。
     * 如果某个成员不存在，则返回 null。
     *
     * @param key     有序集合键名
     * @param members 要查询分数的成员列表
     * @return 分数列表，与输入成员顺序对应，不存在的成员返回null
     */
    default Future<List<@Nullable Double>> getMultipleMemberScores(String key, List<String> members) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(members);

            return api.zmscore(args)
                      .compose(response -> {
                          List<@Nullable Double> scores = new ArrayList<>();
                          response.forEach(item -> {
                              if (item == null) {
                                  scores.add(null);
                              } else {
                                  scores.add(Double.parseDouble(item.toString()));
                              }
                          });
                          return Future.succeededFuture(scores);
                      });
        });
    }

    /**
     * ZPOPMAX key [count]
     * Redis ZPOPMAX 命令用于移除并返回有序集合中分数最高的成员。
     *
     * @param key   有序集合键名
     * @param count 要移除的成员数量（可选，默认为1）
     * @return 被移除的成员和分数，交替出现在列表中
     */
    default Future<List<String>> popMaxMembersFromOrderedSet(String key, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            if (count != null) {
                args.add(count.toString());
            }

            return api.zpopmax(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZPOPMIN key [count]
     * Redis ZPOPMIN 命令用于移除并返回有序集合中分数最低的成员。
     *
     * @param key   有序集合键名
     * @param count 要移除的成员数量（可选，默认为1）
     * @return 被移除的成员和分数，交替出现在列表中
     */
    default Future<List<String>> popMinMembersFromOrderedSet(String key, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            if (count != null) {
                args.add(count.toString());
            }

            return api.zpopmin(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZRANGE key start stop [WITHSCORES]
     * Redis ZRANGE 命令返回有序集中指定索引区间内的成员。
     * 成员按分数值递增(从小到大)排序，分数值相同的成员按字典序排序。
     *
     * @param key        有序集合键名
     * @param start      开始索引，0表示第一个元素，-1表示最后一个元素
     * @param stop       结束索引
     * @param withScores 是否返回成员的分数
     * @return 指定区间内的成员列表，如果withScores为true，则为成员和分数交替的列表
     */
    default Future<List<String>> getOrderedSetRange(String key, long start, long stop, boolean withScores) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(start));
            args.add(String.valueOf(stop));

            if (withScores) {
                args.add("WITHSCORES");
            }

            return api.zrange(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZRANGEBYLEX key min max [LIMIT offset count]
     * Redis ZRANGEBYLEX 命令返回有序集中指定字典序区间内的成员。
     * 所有成员必须具有相同的分数，否则排序就不是按字典序的了。
     *
     * @param key    有序集合键名
     * @param min    字典序范围的最小值，"[member"表示闭区间，"(member"表示开区间，"-"表示无限小
     * @param max    字典序范围的最大值，"[member"表示闭区间，"(member"表示开区间，"+"表示无限大
     * @param offset 可选的分页偏移量
     * @param count  可选的返回成员数量限制
     * @return 符合字典序范围的成员列表
     */
    @Deprecated(since = "4.1.0")
    default Future<List<String>> getOrderedSetRangeByLex(String key, String min, String max, @Nullable Integer offset, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(min);
            args.add(max);

            if (offset != null && count != null) {
                args.add("LIMIT");
                args.add(offset.toString());
                args.add(count.toString());
            }

            return api.zrangebylex(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * Redis ZRANGEBYSCORE 命令返回有序集中指定分数区间内的成员。
     * 成员按分数值递增(从小到大)排序，分数值相同的成员按字典序排序。
     *
     * @param key        有序集合键名
     * @param min        最小分数（包含），使用"(score"表示开区间，可使用"-inf"表示负无穷
     * @param max        最大分数（包含），使用"(score"表示开区间，可使用"+inf"表示正无穷
     * @param withScores 是否返回成员的分数
     * @param offset     可选的分页偏移量
     * @param count      可选的返回成员数量限制
     * @return 符合分数范围的成员列表，如果withScores为true，则为成员和分数交替的列表
     */
    @Deprecated(since = "4.1.0")
    default Future<List<String>> getOrderedSetRangeByScore(String key, String min, String max, boolean withScores, @Nullable Integer offset, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(min);
            args.add(max);

            if (withScores) {
                args.add("WITHSCORES");
            }

            if (offset != null && count != null) {
                args.add("LIMIT");
                args.add(offset.toString());
                args.add(count.toString());
            }

            return api.zrangebyscore(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZRANK key member
     * Redis ZRANK 命令返回有序集中指定成员的排名（从0开始计数），其中有序集成员按分数值递增(从小到大)排序。
     *
     * @param key    有序集合键名
     * @param member 成员名称
     * @return 排名，如果成员不存在则返回null
     */
    default Future<@Nullable Integer> getOrderedSetMemberRank(String key, String member) {
        return api(api -> api.zrank(key, member)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toInteger());
                             }));
    }

    /**
     * ZREM key member [member ...]
     * Redis ZREM 命令用于移除有序集中的一个或多个成员，不存在的成员将被忽略。
     *
     * @param key     有序集合键名
     * @param members 要移除的成员列表
     * @return 实际被移除的成员数量
     */
    default Future<Integer> removeFromOrderedSet(String key, List<String> members) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(members);

            return api.zrem(args)
                      .compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * 移除单个成员的简化版方法
     */
    default Future<Integer> removeFromOrderedSet(String key, String member) {
        return removeFromOrderedSet(key, List.of(member));
    }

    /**
     * ZREMRANGEBYLEX key min max
     * Redis ZREMRANGEBYLEX 命令用于移除有序集中指定字典序区间内的所有成员。
     *
     * @param key 有序集合键名
     * @param min 字典序范围的最小值，"[member"表示闭区间，"(member"表示开区间，"-"表示无限小
     * @param max 字典序范围的最大值，"[member"表示闭区间，"(member"表示开区间，"+"表示无限大
     * @return 被移除的成员数量
     */
    default Future<Integer> removeOrderedSetRangeByLex(String key, String min, String max) {
        return api(api -> api.zremrangebylex(key, min, max)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * ZREMRANGEBYRANK key start stop
     * Redis ZREMRANGEBYRANK 命令用于移除有序集中指定排名区间内的所有成员。
     * 排名区间从0开始，0表示有序集的第一个成员，-1表示最后一个成员。
     *
     * @param key   有序集合键名
     * @param start 开始排名（包含）
     * @param stop  结束排名（包含）
     * @return 被移除的成员数量
     */
    default Future<Integer> removeOrderedSetRangeByRank(String key, long start, long stop) {
        return api(api -> api.zremrangebyrank(key, String.valueOf(start), String.valueOf(stop))
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * ZREMRANGEBYSCORE key min max
     * Redis ZREMRANGEBYSCORE 命令用于移除有序集中指定分数区间内的所有成员。
     *
     * @param key 有序集合键名
     * @param min 最小分数（包含），使用"(score"表示开区间，可使用"-inf"表示负无穷
     * @param max 最大分数（包含），使用"(score"表示开区间，可使用"+inf"表示正无穷
     * @return 被移除的成员数量
     */
    default Future<Integer> removeOrderedSetRangeByScore(String key, String min, String max) {
        return api(api -> api.zremrangebyscore(key, min, max)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * ZREVRANGE key start stop [WITHSCORES]
     * Redis ZREVRANGE 命令返回有序集中指定区间内的成员，成员按分数值递减(从大到小)排列。
     *
     * @param key        有序集合键名
     * @param start      开始索引，0表示第一个元素，-1表示最后一个元素
     * @param stop       结束索引
     * @param withScores 是否返回成员的分数
     * @return 指定区间内的成员列表，如果withScores为true，则为成员和分数交替的列表
     */
    @Deprecated(since = "4.1.0")
    default Future<List<String>> getOrderedSetReverseRange(String key, long start, long stop, boolean withScores) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(start));
            args.add(String.valueOf(stop));

            if (withScores) {
                args.add("WITHSCORES");
            }

            return api.zrevrange(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZREVRANGEBYLEX key max min [LIMIT offset count]
     * Redis ZREVRANGEBYLEX 命令返回有序集中指定字典序区间内的成员，按字典序递减排序。
     * 所有成员必须具有相同的分数，否则排序就不是按字典序的了。
     *
     * @param key    有序集合键名
     * @param max    字典序范围的最大值，"[member"表示闭区间，"(member"表示开区间，"+"表示无限大
     * @param min    字典序范围的最小值，"[member"表示闭区间，"(member"表示开区间，"-"表示无限小
     * @param offset 可选的分页偏移量
     * @param count  可选的返回成员数量限制
     * @return 符合字典序范围的成员列表，按字典序递减排序
     */
    @Deprecated(since = "4.1.0")
    default Future<List<String>> getOrderedSetReverseRangeByLex(String key, String max, String min, @Nullable Integer offset, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(max);
            args.add(min);

            if (offset != null && count != null) {
                args.add("LIMIT");
                args.add(offset.toString());
                args.add(count.toString());
            }

            return api.zrevrangebylex(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * Redis ZREVRANGEBYSCORE 命令返回有序集中指定分数区间内的成员，按分数递减排序。
     *
     * @param key        有序集合键名
     * @param max        最大分数（包含），使用"(score"表示开区间，可使用"+inf"表示正无穷
     * @param min        最小分数（包含），使用"(score"表示开区间，可使用"-inf"表示负无穷
     * @param withScores 是否返回成员的分数
     * @param offset     可选的分页偏移量
     * @param count      可选的返回成员数量限制
     * @return 符合分数范围的成员列表，按分数递减排序，如果withScores为true，则为成员和分数交替的列表
     */
    @Deprecated(since = "4.1.0")
    default Future<List<String>> getOrderedSetReverseRangeByScore(String key, String max, String min, boolean withScores, @Nullable Integer offset, @Nullable Integer count) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(max);
            args.add(min);

            if (withScores) {
                args.add("WITHSCORES");
            }

            if (offset != null && count != null) {
                args.add("LIMIT");
                args.add(offset.toString());
                args.add(count.toString());
            }

            return api.zrevrangebyscore(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZREVRANK key member
     * Redis ZREVRANK 命令返回有序集中成员的排名，其中有序集成员按分数值递减(从大到小)排序。
     *
     * @param key    有序集合键名
     * @param member 成员名称
     * @return 排名，如果成员不存在则返回null
     */
    default Future<@Nullable Integer> getOrderedSetMemberReverseRank(String key, String member) {
        return api(api -> api.zrevrank(key, member)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toInteger());
                             }));
    }

    /**
     * ZSCAN key cursor [MATCH pattern] [COUNT count]
     * Redis ZSCAN 命令用于迭代有序集合中的元素。
     *
     * @param key     有序集合键名
     * @param cursor  游标值（首次调用使用0）
     * @param pattern 可选的匹配模式
     * @param count   可选的单次迭代返回的元素数量
     * @return 包含下一个游标值和当前批次元素的结果
     */
    default Future<ZScanResult> scanOrderedSet(String key, String cursor, String pattern, Integer count) {
        return zscan(key, cursor, pattern, count);
    }

    /**
     * ZSCORE key member
     * Redis ZSCORE 命令返回有序集中成员的分数值。
     *
     * @param key    有序集合键名
     * @param member 成员名称
     * @return 成员的分数值，如果成员不存在或键不存在则返回null
     */
    default Future<Double> getOrderedSetMemberScore(String key, String member) {
        return api(api -> api.zscore(key, member)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(Double.parseDouble(response.toString()));
                             }));
    }

    /**
     * ZUNION numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
     * Redis ZUNION 命令计算给定的一个或多个有序集的并集，并返回结果。
     *
     * @param keys       要计算并集的有序集合键名列表
     * @param weights    可选的权重列表，与输入键顺序对应
     * @param aggregate  可选的聚合方式（SUM、MIN、MAX）
     * @param withScores 是否返回分数
     * @return 计算结果成员列表，如果withScores为true，则为成员和分数交替的列表
     */
    default Future<List<String>> unionOrderedSets(List<String> keys, List<Double> weights, String aggregate, boolean withScores) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(String.valueOf(keys.size()));
            args.addAll(keys);

            if (weights != null && !weights.isEmpty()) {
                args.add("WEIGHTS");
                for (Double weight : weights) {
                    args.add(String.valueOf(weight));
                }
            }

            if (aggregate != null && !aggregate.isEmpty()) {
                args.add("AGGREGATE");
                args.add(aggregate);
            }

            if (withScores) {
                args.add("WITHSCORES");
            }

            return api.zunion(args)
                      .compose(response -> {
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
     * Redis ZUNIONSTORE 命令计算给定的一个或多个有序集的并集，并将结果存储到新的有序集合 destination 中。
     *
     * @param destination 目标有序集合键名
     * @param keys        要计算并集的有序集合键名列表
     * @param weights     可选的权重列表，与输入键顺序对应
     * @param aggregate   可选的聚合方式（SUM、MIN、MAX）
     * @return 目标有序集合中的元素数量
     */
    default Future<Integer> storeUnionOfOrderedSets(String destination, List<String> keys, List<Double> weights, String aggregate) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(destination);
            args.add(String.valueOf(keys.size()));
            args.addAll(keys);

            if (weights != null && !weights.isEmpty()) {
                args.add("WEIGHTS");
                for (Double weight : weights) {
                    args.add(String.valueOf(weight));
                }
            }

            if (aggregate != null && !aggregate.isEmpty()) {
                args.add("AGGREGATE");
                args.add(aggregate);
            }

            return api.zunionstore(args)
                      .compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * BZPOPMAX key [key ...] timeout
     * Redis BZPOPMAX 命令是 ZPOPMAX 命令的阻塞版本，它会在没有元素可弹出时阻塞连接。
     *
     * @param keys    要弹出元素的有序集合键名列表
     * @param timeout 超时时间（秒），0表示永不超时
     * @return 包含三个元素的列表：键名、弹出的成员和分数；超时返回null
     */
    default Future<List<String>> blockingPopMaxFromOrderedSets(List<String> keys, long timeout) {
        return api(api -> {
            List<String> args = new ArrayList<>(keys);
            args.add(String.valueOf(timeout));

            return api.bzpopmax(args)
                      .compose(response -> {
                          if (response == null) {
                              return Future.succeededFuture(null);
                          }
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }

    /**
     * BZPOPMIN key [key ...] timeout
     * Redis BZPOPMIN 命令是 ZPOPMIN 命令的阻塞版本，它会在没有元素可弹出时阻塞连接。
     *
     * @param keys    要弹出元素的有序集合键名列表
     * @param timeout 超时时间（秒），0表示永不超时
     * @return 包含三个元素的列表：键名、弹出的成员和分数；超时返回null
     */
    default Future<List<String>> blockingPopMinFromOrderedSets(List<String> keys, long timeout) {
        return api(api -> {
            List<String> args = new ArrayList<>(keys);
            args.add(String.valueOf(timeout));

            return api.bzpopmin(args)
                      .compose(response -> {
                          if (response == null) {
                              return Future.succeededFuture(null);
                          }
                          List<String> result = new ArrayList<>();
                          response.forEach(item -> result.add(item.toString()));
                          return Future.succeededFuture(result);
                      });
        });
    }
}
