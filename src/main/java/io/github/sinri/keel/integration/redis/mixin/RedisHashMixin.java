package io.github.sinri.keel.integration.redis.mixin;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis API 调用中 Hash 相关的 Mixin。
 *
 * @since 5.0.0
 */
public interface RedisHashMixin extends RedisApiMixin {
    /**
     * HDEL key field [field ...]
     * Redis HDEL 命令用于删除哈希表 key 中的一个或多个指定字段，不存在的字段将被忽略。
     *
     * @return 被成功删除字段的数量，不包括被忽略的字段。
     */
    default Future<Integer> deleteHashField(String key, List<String> fields) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(fields);
            return api.hdel(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    default Future<Integer> deleteHashField(String key, String field) {
        return deleteHashField(key, List.of(field));
    }

    /**
     * HEXISTS key field
     * Redis HEXISTS 命令用于查看哈希表 key 中，指定的字段是否存在。
     *
     * @return 如果存在返回 true，不存在返回 false。
     */
    default Future<Boolean> existsHashField(String key, String field) {
        return api(api -> api.hexists(key, field)
                             .compose(response -> Future.succeededFuture(response.toInteger() == 1)));
    }

    /**
     * HGET key field
     * Redis HGET 命令用于返回哈希表中指定字段的值。
     *
     * @return 返回给定字段的值。如果给定的字段或 key 不存在时，返回 null。
     */
    default Future<String> getHashField(String key, String field) {
        return api(api -> api.hget(key, field).compose(response -> {
            if (response == null) {
                return Future.succeededFuture(null);
            }
            return Future.succeededFuture(response.toString());
        }));
    }

    /**
     * HGETALL key
     * Redis HGETALL 命令用于返回哈希表中，所有的字段和值。
     * 在返回值里，紧跟每个字段名(field name)之后是字段的值(value)，所以返回值的长度是哈希表大小的两倍。
     *
     * @return 以Map形式返回哈希表的字段及字段值。若 key 不存在，返回空Map。
     */
    default Future<Map<String, String>> getAllHashFields(String key) {
        return api(api -> api.hgetall(key).compose(response -> {
            Map<String, String> map = new HashMap<>();
            if (response == null) {
                return Future.succeededFuture(map);
            }

            for (int i = 0; i < response.size(); i += 2) {
                if (i + 1 < response.size()) {
                    map.put(response.get(i).toString(), response.get(i + 1).toString());
                }
            }

            return Future.succeededFuture(map);
        }));
    }

    /**
     * HINCRBY key field increment
     * Redis HINCRBY 命令用于为哈希表中的字段值加上指定增量值。
     * 增量也可以为负数，相当于对指定字段进行减法操作。
     * 如果哈希表的 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
     * 如果指定的字段不存在，那么在执行命令前，字段的值被初始化为 0 。
     * 对一个储存字符串值的字段执行 HINCRBY 命令将造成一个错误。
     *
     * @return 执行 HINCRBY 命令之后，哈希表中字段的值。
     */
    default Future<Long> incrementHashField(String key, String field, long increment) {
        return api(api -> api.hincrby(key, field, String.valueOf(increment))
                             .compose(response -> Future.succeededFuture(response.toLong())));
    }

    /**
     * HINCRBYFLOAT key field increment
     * Redis HINCRBYFLOAT 命令用于为哈希表中的字段值加上指定浮点数增量值。
     * 如果指定的字段不存在，那么在执行命令前，字段的值被初始化为 0 。
     *
     * @return 执行 HINCRBYFLOAT 命令之后，哈希表中字段的值。
     */
    default Future<Double> incrementHashFieldByFloat(String key, String field, double increment) {
        return api(api -> api.hincrbyfloat(key, field, String.valueOf(increment))
                             .compose(response -> Future.succeededFuture(Double.parseDouble(response.toString()))));
    }

    /**
     * HKEYS key
     * Redis HKEYS 命令用于获取哈希表中的所有字段名。
     *
     * @return 包含哈希表中所有字段的列表。当 key 不存在时，返回一个空列表。
     */
    default Future<List<String>> getHashKeys(String key) {
        return api(api -> api.hkeys(key).compose(response -> {
            List<String> list = new ArrayList<>();
            if (response != null) {
                response.forEach(item -> list.add(item.toString()));
            }
            return Future.succeededFuture(list);
        }));
    }

    /**
     * HLEN key
     * Redis HLEN 命令用于获取哈希表中字段的数量。
     *
     * @return 哈希表中字段的数量。当 key 不存在时，返回 0。
     */
    default Future<Integer> getHashLength(String key) {
        return api(api -> api.hlen(key).compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * HMGET key field [field ...]
     * Redis HMGET 命令用于返回哈希表中，一个或多个给定字段的值。
     * 如果指定的字段不存在于哈希表，那么返回一个 null 值。
     *
     * @return 一个包含多个给定字段关联值的表，表值的排列顺序和指定字段的请求顺序一样。
     */
    default Future<List<String>> getMultipleHashFields(String key, List<String> fields) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.addAll(fields);
            return api.hmget(args).compose(response -> {
                List<String> list = new ArrayList<>();
                if (response != null) {
                    response.forEach(item -> {
                        if (item == null) {
                            list.add(null);
                        } else {
                            list.add(item.toString());
                        }
                    });
                }
                return Future.succeededFuture(list);
            });
        });
    }

    /**
     * HMSET key field value [field value ...]
     * Redis HMSET 命令用于同时将多个 field-value (字段-值)对设置到哈希表 key 中。
     * 此命令会覆盖哈希表中已存在的字段。
     * 如果 key 不存在，会创建一个空哈希表，并执行 HMSET 操作。
     */
    @Deprecated(since = "4.1.0")
    default Future<Void> setMultipleHashFields(String key, Map<String, String> fieldValues) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            fieldValues.forEach((field, value) -> {
                args.add(field);
                args.add(value);
            });
            return api.hmset(args).compose(response -> {
                if ("OK".equals(response.toString())) {
                    return Future.succeededFuture();
                } else {
                    return Future.failedFuture(new RuntimeException("HMSET failed with: " + response));
                }
            });
        });
    }

    /**
     * HSCAN key cursor [MATCH pattern] [COUNT count]
     * Redis HSCAN 命令用于迭代哈希表中的键值对。
     *
     * @param key     哈希表的键
     * @param cursor  游标
     * @param pattern 匹配的模式
     * @param count   指定从数据集里返回多少元素，默认值为 10
     * @return 包含两个元素的列表，第一个元素是用于下一次迭代的新游标，第二个元素是包含匹配元素的列表
     */
    default Future<Map<String, Object>> scanHash(String key, String cursor, String pattern, Integer count) {
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
            return api.hscan(args).compose(response -> {
                Map<String, Object> result = new HashMap<>();
                if (response != null && response.size() >= 2) {
                    result.put("cursor", response.get(0).toString());

                    List<String> elements = new ArrayList<>();
                    if (response.get(1) != null) {
                        response.get(1).forEach(item -> elements.add(item.toString()));
                    }
                    result.put("elements", elements);
                }
                return Future.succeededFuture(result);
            });
        });
    }

    /**
     * HSET key field value [field value ...]
     * Redis HSET 命令用于为哈希表中的字段赋值。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果字段已经存在于哈希表中，旧值将被覆盖。
     *
     * @return 如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1。如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0。
     */
    default Future<Integer> setHashField(String key, String field, String value) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(field);
            args.add(value);
            return api.hset(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * HSET key field value [field value ...]
     * Redis HSET 命令用于为哈希表中的字段赋值。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果字段已经存在于哈希表中，旧值将被覆盖。
     *
     * @return 添加的字段数量
     */
    default Future<Integer> setHashFields(String key, Map<String, String> fieldValues) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            fieldValues.forEach((field, value) -> {
                args.add(field);
                args.add(value);
            });
            return api.hset(args).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * HSETNX key field value
     * Redis HSETNX 命令用于为哈希表中不存在的字段赋值。
     * 如果哈希表不存在，一个新的哈希表被创建并进行 HSETNX 操作。
     * 如果字段已经存在于哈希表中，操作无效。
     *
     * @return 设置成功，返回 1。如果给定字段已经存在且没有操作被执行，返回 0。
     */
    default Future<Integer> setHashFieldIfNotExists(String key, String field, String value) {
        return api(api -> api.hsetnx(key, field, value)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * HSTRLEN key field
     * Redis HSTRLEN 命令用于返回哈希表中字段值的字符串长度。
     * 如果字段或者 key 不存在，返回 0。
     *
     * @return 返回字段值的字符串长度。
     */
    default Future<Integer> getHashFieldValueLength(String key, String field) {
        return api(api -> api.hstrlen(key, field).compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * HVALS key
     * Redis HVALS 命令返回哈希表所有字段的值。
     *
     * @return 一个包含哈希表中所有值的列表。当 key 不存在时，返回一个空列表。
     */
    default Future<List<String>> getHashValues(String key) {
        return api(api -> api.hvals(key).compose(response -> {
            List<String> list = new ArrayList<>();
            if (response != null) {
                response.forEach(item -> list.add(item.toString()));
            }
            return Future.succeededFuture(list);
        }));
    }
}
