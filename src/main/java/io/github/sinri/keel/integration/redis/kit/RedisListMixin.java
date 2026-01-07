package io.github.sinri.keel.integration.redis.kit;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Redis API 调用中 List 相关的 Mixin。
 *
 * @since 5.0.0
 */
@NullMarked
interface RedisListMixin extends RedisApiMixin {

    default Future<Integer> pushToListTail(String key, String element) {
        return pushToListTail(key, List.of(element));
    }

    /**
     * RPUSH key element [element ...]
     * 向存存储在 key 中的列表的尾部插入所有指定的值。
     * 如果 key 不存在，那么会创建一个空的列表然后再进行 push 操作。
     * 当 key 保存的不是列表，那么会返回一个错误。
     *
     * @return 执行 push 操作后的列表长度。
     */
    default Future<Integer> pushToListTail(String key, List<String> elements) {
        return api(api -> {
            List<String> list = new ArrayList<>();
            list.add(key);
            list.addAll(elements);
            return api.rpush(list).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * RPUSHX key element [element ...]
     * RPUSHX将值 value 插入到列表 key 的表尾, 当且仅当 key 存在并且是一个列表。 和 RPUSH命令相反, 当 key 不存在时，RPUSHX 命令什么也不做。
     *
     * @return RPUSHX 命令执行之后列表的长度。
     */
    default Future<Integer> pushToExistedListTail(String key, List<String> elements) {
        return api(api -> {
            List<String> list = new ArrayList<>();
            list.add(key);
            list.addAll(elements);
            return api.rpushx(list).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    default Future<Integer> pushToExistedListTail(String key, String element) {
        return pushToExistedListTail(key, List.of(element));
    }


    default Future<Integer> pushToListHead(String key, String element) {
        return pushToListHead(key, List.of(element));
    }

    /**
     * LPUSH key element [element ...]
     * 将一个或多个值插入到列表key 的头部。
     * 如果 key 不存在，那么在进行 push 操作前会创建一个空列表。
     * 如果 key 对应的值不是 list 类型，那么会返回一个错误。
     */
    default Future<Integer> pushToListHead(String key, List<String> elements) {
        return api(api -> {
            List<String> list = new ArrayList<>();
            list.add(key);
            list.addAll(elements);
            return api.lpush(list).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    /**
     * LPUSHX key element [element ...]
     * Redis LPUSHX 在当 key 存在并且存储着一个 list 类型值的时候，向值 list 的头部插入 value。
     * 与 LPUSH 相反，当 key 不存在的时候不会进行任何操作。
     *
     * @return 执行push操作后列表list的长度。
     *         REDIS &gt;= 4.0: 支持一次插入多个值。老版本一次只能插入一个值。
     */
    default Future<Integer> pushToExistedListHead(String key, List<String> elements) {
        return api(api -> {
            List<String> list = new ArrayList<>();
            list.add(key);
            list.addAll(elements);
            return api.lpushx(list).compose(response -> Future.succeededFuture(response.toInteger()));
        });
    }

    default Future<Integer> pushToExistedListHead(String key, String element) {
        return pushToExistedListHead(key, List.of(element));
    }

    /**
     * LLEN key
     * Redis LLEN 用于返回存储在 key 中的列表长度。
     * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 。
     * 如果 key 不是列表类型，返回一个错误。
     *
     * @return 用于返回存储在 key 中的列表长度。
     */
    default Future<Integer> getListLength(String key) {
        return api(api -> api.llen(key).compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * LPOP key
     * Redis LPOP 命令用于删除并返回存储在 key 中的列表的第一个元素。
     *
     * @return 列表的首元素，key 不存在的时候返回 nil 。
     */
    default Future<String> popFromListHead(String key) {
        return api(api -> api.lpop(List.of(key)).compose(response -> {
            if (response == null) {
                return Future.succeededFuture(null);
            }
            return Future.succeededFuture(response.toString());
        }));
    }

    /**
     * RPOP key
     * Redis RPOP 用于移除并返回列表 key 的最后一个元素。
     *
     * @return 最后一个元素的值，key 不存在时返回 nil 。
     */
    default Future<String> popFromListTail(String key) {
        return api(api -> api.rpop(List.of(key)).compose(response -> {
            if (response == null) {
                return Future.succeededFuture(null);
            }
            return Future.succeededFuture(response.toString());
        }));
    }

    /**
     * LTRIM key start stop
     * Redis LTRIM 用于修剪(trim)一个已存在的 list，这样 list 就会只包含指定范围的指定元素。start 和 stop 都是由0开始计数的， 这里的 0 是列表里的第一个元素（表头），1
     * 是第二个元素，以此类推。
     * start 和 end 也可以用负数来表示与表尾的偏移量，比如 -1 表示列表里的最后一个元素， -2 表示倒数第二个，等等。
     * 超过范围的下标并不会产生错误：如果 start 超过列表尾部，或者 start &gt; end，结果会是列表变成空表（即该 key 会被移除）。 如果 end 超过列表尾部，Redis 会将其当作列表的最后一个元素。
     */
    default Future<Void> trimList(String key, int start, int stop) {
        return api(api -> api.ltrim(key, String.valueOf(start), String.valueOf(stop))
                             .compose(response -> {
                                 Objects.requireNonNull(response);
                                 if (Objects.equals("OK", response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException("NOT OK but " + response));
                                 }
                             }));
    }

    /**
     * LRANGE key start stop
     * Redis LRANGE 用于返回列表中指定区间内的元素，区间以偏移量 START 和 END 指定。
     * 其中 0 表示列表的第一个元素， 1 表示列表的第二个元素，以此类推。 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     *
     * @return 一个列表，包含指定区间内的元素。
     */
    default Future<List<String>> fetchListWithRange(String key, int start, int stop) {
        return api(api -> api.lrange(key, String.valueOf(start), String.valueOf(stop))
                             .compose(response -> {
                                 List<String> list = new ArrayList<>();
                                 response.forEach(response1 -> list.add(response1.toString()));

                                 return Future.succeededFuture(list);
                             }));
    }

    /**
     * LINDEX key index
     * LINDEX 返回列表 key 里索引 index 位置存储的元素。 index 下标是从 0 开始索引的，所以 0 是表示第一个元素， 1 表示第二个元素，并以此类推。
     * 负数索引用于指定从列表尾部开始索引的元素，在这种方法下，-1 表示最后一个元素，-2 表示倒数第二个元素，并以此往前推。
     * 当 key 值不是列表的时候，会返回错误。
     *
     * @return 查询的元素，index 超出索引范围时返回 nil 。
     */
    default Future<String> getElementInList(String key, int index) {
        return api(api -> api.lindex(key, String.valueOf(index))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture();
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * LINSERT key BEFORE|AFTER pivot element
     * 把 element 插入到列表 key 中参考值 pivot 的前面或后面。
     * 当 key 不存在时，这个list会被看作是空list，什么都不执行。
     * 当 key 存在，值不是列表类型时，返回错误。
     *
     * @return 执行操作后的列表长度，列表中pivot参考值不存在的时候返回 -1。
     */
    default Future<Integer> insertIntoListBefore(String key, String pivot, String element) {
        return api(api -> api.linsert(key, "BEFORE", pivot, element)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * LPOS key element [RANK rank] [COUNT num-matches] [MAXLEN len]
     * Redis LPOS 命令返回列表 key 中匹配给定 element 成员的索引。
     * 默认的，当没有其它参数选项时，LPOS 从列表头部开始扫描，直到列表尾部，查找第一个与匹配"element"的成员。
     * 如果找到匹配的成员，返回这个成员的索引（从零开计数），如果找不到匹配的成员返回 NULL。
     *
     * @param count  返回几个匹配的元素,COUNT 为 0 时表示返回所有匹配成员的索引数组。
     * @param rank   返回第几个匹配的元素(since 1, or -1),负值 RANK 参数表示换一个搜索方向，从列表尾部想列表头部搜索。
     * @param maxLen 只查找最多 maxLen 个成员。
     */
    default Future<List<Integer>> seekElementInList(
            String key,
            String element,
            @Nullable Integer rank,
            @Nullable Integer count,
            @Nullable Integer maxLen
    ) {
        return api(api -> {
            List<String> args = new ArrayList<>();

            args.add(key);
            args.add(element);
            if (rank != null && rank != 0) {
                args.add("RANK");
                args.add(String.valueOf(rank));
            }
            if (count != null) {
                args.add("COUNT");
                args.add(String.valueOf(count));
            }
            if (maxLen != null) {
                args.add("MAXLEN");
                args.add(String.valueOf(maxLen));
            }

            return api.lpop(args).compose(response -> {
                ArrayList<Integer> list = new ArrayList<>();
                if (count == null) {
                    if (response != null) {
                        list.add(response.toInteger());
                    }
                } else {
                    response.forEach(x -> list.add(x.toInteger()));
                }
                return Future.succeededFuture(list);
            });
        });
    }

    default Future<Integer> seekFirstElementInList(String key, String element) {
        return seekElementInList(key, element, 1, null, null)
                .compose(indices -> {
                    if (indices.isEmpty()) {
                        return Future.succeededFuture();
                    } else {
                        return Future.succeededFuture(indices.get(0));
                    }
                });
    }

    default Future<Integer> seekLastElementInList(String key, String element) {
        return seekElementInList(key, element, -1, null, null)
                .compose(indices -> {
                    if (indices.isEmpty()) {
                        return Future.succeededFuture();
                    } else {
                        return Future.succeededFuture(indices.get(0));
                    }
                });
    }

    /**
     * LREM key count element
     * Redis LREM 用于从列表 key 中删除前 count 个值等于 element 的元素。 这个 count 参数通过下面几种方式影响这个操作：
     * count &gt; 0: 从头到尾删除值为 value 的元素。
     * count &lt; 0: 从尾到头删除值为 value 的元素。
     * count = 0: 移除所有值为 value 的元素。
     * 比如， LREM list -2 "hello" 会从列表key中删除最后两个出现的 "hello"。
     * 需要注意的是，不存在key会被当作空list处理，所以当 key 不存在的时候，这个命令会返回 0。
     *
     * @return 删除元素个数。
     */
    default Future<Integer> removeSomeMatchedElementsFromList(String key, int count, String element) {
        return api(api -> api.lrem(key, String.valueOf(count), element)
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * LSET key index element
     * Redis LSET 用于设置列表 key 中 index 位置的元素值为 element。 更多关于 index 参数的信息，详见 LINDEX。
     * 当 index 超出列表索引范围时会返回错误ERR ERR index out of range。
     */
    default Future<Void> setElementInList(String key, int index, String element) {
        return api(api -> api.lset(key, String.valueOf(index), element)
                             .compose(response -> {
                                 if ("OK".equals(response.toString())) {
                                     return Future.succeededFuture();
                                 } else {
                                     return Future.failedFuture(new RuntimeException("not OK but " + response));
                                 }
                             }));
    }

    /**
     * BLMOVE source destination LEFT|RIGHT LEFT|RIGHT timeout
     * Redis BLMOVE 是 LMOVE 命令的阻塞版本。
     * 当 source 包含元素时，这个命令表现得和 LMOVE 一样。
     * 当 source 为空时， Redis 将会阻塞这个连接，直到另一个客户端 push 元素进入这个列表或者达到 timeout 时限。
     * timeout 为 0 表示无限阻塞。
     *
     * @param source      源列表
     * @param destination 目标列表
     * @param from        从哪里移出元素，可以是 "LEFT" 或 "RIGHT"
     * @param to          移动到哪里，可以是 "LEFT" 或 "RIGHT"
     * @param timeout     超时时间（秒），0 表示无限等待
     * @return 被移动的元素；如果超时返回 null
     */
    default Future<String> blockingMoveElementBetweenLists(String source, String destination, String from, String to, long timeout) {
        return api(api -> api.blmove(source, destination, from, to, String.valueOf(timeout))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * BLPOP key [key ...] timeout
     * Redis BLPOP 是 LPOP 命令的阻塞版本。
     * 当给定列表内没有任何元素可供弹出时，连接将被 BLPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
     * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。
     *
     * @param keys    一个或多个列表键
     * @param timeout 超时时间（秒），0 表示无限等待
     * @return 如果列表为空，返回 null；否则返回一个包含两个元素的 List，第一个是键名，第二个是弹出的元素
     */
    default Future<List<String>> blockingPopFromListsHead(List<String> keys, long timeout) {
        return api(api -> {
            List<String> args = new ArrayList<>(keys);
            args.add(String.valueOf(timeout));
            return api.blpop(args).compose(response -> {
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
     * BRPOP key [key ...] timeout
     * Redis BRPOP 是 RPOP 命令的阻塞版本。
     * 当给定列表内没有任何元素可供弹出时，连接将被 BRPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
     * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的尾元素。
     *
     * @param keys    一个或多个列表键
     * @param timeout 超时时间（秒），0 表示无限等待
     * @return 如果列表为空，返回 null；否则返回一个包含两个元素的 List，第一个是键名，第二个是弹出的元素
     */
    default Future<List<String>> blockingPopFromListsTail(List<String> keys, long timeout) {
        return api(api -> {
            List<String> args = new ArrayList<>(keys);
            args.add(String.valueOf(timeout));
            return api.brpop(args).compose(response -> {
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
     * BRPOPLPUSH source destination timeout
     * Redis BRPOPLPUSH 是 RPOPLPUSH 的阻塞版本。
     * 当 source 中没有元素时，BRPOPLPUSH 命令会阻塞连接，直到超时或有元素可以被弹出。
     * 超时设置为 0 表示无限期阻塞。
     *
     * @param source      源列表
     * @param destination 目标列表
     * @param timeout     超时时间（秒），0 表示无限等待
     * @return 弹出并推入的元素；如果超时返回 null
     * @deprecated use {@link RedisListMixin#blockingMoveElementBetweenLists(String, String, String, String, long)}
     */
    @Deprecated(since = "5.0.0")
    default Future<String> blockingPopTailAndPushHead(String source, String destination, long timeout) {
        return api(api -> api.brpoplpush(source, destination, String.valueOf(timeout))
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * LMOVE source destination LEFT|RIGHT LEFT|RIGHT
     * Redis LMOVE 用于原子性地将元素从一个列表移动到另一个列表。
     * LMOVE 会从 source 列表的头部（LEFT）或尾部（RIGHT）弹出一个元素，
     * 并将这个元素推入到 destination 列表的头部（LEFT）或尾部（RIGHT）。
     *
     * @param source      源列表
     * @param destination 目标列表
     * @param from        从哪里移出元素，可以是 "LEFT" 或 "RIGHT"
     * @param to          移动到哪里，可以是 "LEFT" 或 "RIGHT"
     * @return 被移动的元素；如果 source 为空，返回 null
     */
    default Future<String> moveElementBetweenLists(String source, String destination, String from, String to) {
        return api(api -> api.lmove(source, destination, from, to)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }

    /**
     * RPOPLPUSH source destination
     * Redis RPOPLPUSH 命令用于移除列表 source 的最后一个元素，并将该元素添加到列表 destination 的头部，
     * 并返回这个被移除的元素。
     * 如果 source 不存在，那么会返回 nil，并且不会执行其他操作。
     * 如果 source 和 destination 相同，则相当于将列表中的最后一个元素移到列表头部。
     *
     * @param source      源列表
     * @param destination 目标列表
     * @return 被移除和添加的元素；如果列表 source 为空，返回 null
     * @deprecated use {@link RedisListMixin#moveElementBetweenLists(String, String, String, String)}
     */
    @Deprecated(since = "5.0.0")
    default Future<String> popTailAndPushHead(String source, String destination) {
        return api(api -> api.rpoplpush(source, destination)
                             .compose(response -> {
                                 if (response == null) {
                                     return Future.succeededFuture(null);
                                 }
                                 return Future.succeededFuture(response.toString());
                             }));
    }
}
