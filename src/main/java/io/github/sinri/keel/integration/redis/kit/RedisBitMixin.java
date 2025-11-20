package io.github.sinri.keel.integration.redis.kit;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Redis API 调用中 Bit 相关的 Mixin。
 *
 * @since 5.0.0
 */
public interface RedisBitMixin extends RedisApiMixin {
    /**
     * BITCOUNT key [start end]
     * 计算给定字符串中，被设置为 1 的比特位的数量。
     * 默认情况下，BITCOUNT 会计算整个字符串的比特位。通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
     * 注意：start 和 end 参数都可以接受负数值。负数的起始索引（start）和终止索引（end）表示从字符串的末尾开始计数。
     * 比如说， -1 表示最后一个字节， -2 表示倒数第二个字节，以此类推。
     *
     * @param key 字符串键
     * @return 被设置为 1 的位的数量
     */
    default Future<Long> bitCount(String key) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            return api.bitcount(args).compose(response -> Future.succeededFuture(response.toLong()));
        });
    }

    /**
     * BITCOUNT key [start end]
     * 计算给定字符串中，被设置为 1 的比特位的数量。
     * 通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
     * 注意：start 和 end 参数都可以接受负数值。负数的起始索引（start）和终止索引（end）表示从字符串的末尾开始计数。
     * 比如说， -1 表示最后一个字节， -2 表示倒数第二个字节，以此类推。
     *
     * @param key   字符串键
     * @param start 起始字节位置
     * @param end   结束字节位置
     * @return 被设置为 1 的位的数量
     */
    default Future<Long> bitCount(String key, long start, long end) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(start));
            args.add(String.valueOf(end));
            return api.bitcount(args).compose(response -> Future.succeededFuture(response.toLong()));
        });
    }

    /**
     * BITFIELD key [GET type offset] [SET type offset value] [INCRBY type offset increment] [OVERFLOW WRAP|SAT|FAIL]
     * 此命令非常复杂，允许对字符串进行位级别的操作，可以获取、设置和递增特定位宽的整数值。
     * 由于此命令功能复杂，且具体参数组合较多，这里提供基本的GET子命令实现。
     *
     * @param key    键名
     * @param type   整数类型，形如 i5 表示有符号5位整数，u4 表示无符号4位整数
     * @param offset 位偏移量
     * @return 获取的整数值
     */
    default Future<Long> bitfieldGet(String key, String type, long offset) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add("GET");
            args.add(type);
            args.add(String.valueOf(offset));
            return api.bitfield(args).compose(response -> {
                if (response == null || response.size() == 0) {
                    return Future.succeededFuture(0L);
                }
                return Future.succeededFuture(response.get(0).toLong());
            });
        });
    }

    /**
     * BITFIELD key [GET type offset] [SET type offset value] [INCRBY type offset increment] [OVERFLOW WRAP|SAT|FAIL]
     * 设置子命令实现
     *
     * @param key    键名
     * @param type   整数类型，形如 i5 表示有符号5位整数，u4 表示无符号4位整数
     * @param offset 位偏移量
     * @param value  要设置的值
     * @return 操作之前存储在该位置的值
     */
    default Future<Long> bitfieldSet(String key, String type, long offset, long value) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add("SET");
            args.add(type);
            args.add(String.valueOf(offset));
            args.add(String.valueOf(value));
            return api.bitfield(args).compose(response -> {
                if (response == null || response.size() == 0) {
                    return Future.succeededFuture(0L);
                }
                return Future.succeededFuture(response.get(0).toLong());
            });
        });
    }

    /**
     * BITOP operation destkey key [key ...]
     * 对一个或多个保存二进制位的字符串key进行位元操作，并将结果保存到 destkey 上。
     * operation 可以是 AND、OR、XOR、NOT 这四种操作中的任意一种：
     * - BITOP AND destkey key [key ...] ：对一个或多个 key 求逻辑并，并将结果保存到 destkey 。
     * - BITOP OR destkey key [key ...] ：对一个或多个 key 求逻辑或，并将结果保存到 destkey 。
     * - BITOP XOR destkey key [key ...] ：对一个或多个 key 求逻辑异或，并将结果保存到 destkey 。
     * - BITOP NOT destkey key ：对给定 key 求逻辑非，并将结果保存到 destkey 。
     * <p>
     * 除了 NOT 操作之外，其他操作都可以接受一个或多个 key 作为输入。
     *
     * @param operation 位操作类型
     * @param destkey   目标键
     * @param keys      源键列表
     * @return 保存到目标键的字符串的长度，单位为字节
     */
    default Future<Long> bitOp(String operation, String destkey, List<String> keys) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(operation);
            args.add(destkey);
            args.addAll(keys);
            return api.bitop(args).compose(response -> Future.succeededFuture(response.toLong()));
        });
    }

    /**
     * BITOP NOT destkey key
     * 对给定 key 求逻辑非，并将结果保存到 destkey
     *
     * @param destkey 目标键
     * @param key     源键
     * @return 保存到目标键的字符串的长度，单位为字节
     */
    default Future<Long> bitOpNot(String destkey, String key) {
        return bitOp("NOT", destkey, Collections.singletonList(key));
    }

    /**
     * BITOP AND destkey key [key ...]
     * 对一个或多个 key 求逻辑并，并将结果保存到 destkey
     *
     * @param destkey 目标键
     * @param keys    源键列表
     * @return 保存到目标键的字符串的长度，单位为字节
     */
    default Future<Long> bitOpAnd(String destkey, String... keys) {
        return bitOp("AND", destkey, Arrays.asList(keys));
    }

    /**
     * BITOP OR destkey key [key ...]
     * 对一个或多个 key 求逻辑或，并将结果保存到 destkey
     *
     * @param destkey 目标键
     * @param keys    源键列表
     * @return 保存到目标键的字符串的长度，单位为字节
     */
    default Future<Long> bitOpOr(String destkey, String... keys) {
        return bitOp("OR", destkey, Arrays.asList(keys));
    }

    /**
     * BITOP XOR destkey key [key ...]
     * 对一个或多个 key 求逻辑异或，并将结果保存到 destkey
     *
     * @param destkey 目标键
     * @param keys    源键列表
     * @return 保存到目标键的字符串的长度，单位为字节
     */
    default Future<Long> bitOpXor(String destkey, String... keys) {
        return bitOp("XOR", destkey, Arrays.asList(keys));
    }

    /**
     * BITPOS key bit [start] [end]
     * 返回字符串里面第一个被设置为1或者0的bit位。
     * start 和 end 参数可以指定查找的范围。
     * BITPOS key 0, 即寻找bit=0的位置会发生特殊处理，如果所有位都是1，则返回字符串长度*8（即下一个将会被分配的位）
     *
     * @param key 键名
     * @param bit 要查找的位值（0或1）
     * @return 字符串第一个被设置为bit的bit位，如果没找到，返回-1
     */
    default Future<Long> bitPos(String key, int bit) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(bit));
            return api.bitpos(args).compose(response -> Future.succeededFuture(response.toLong()));
        });
    }

    /**
     * BITPOS key bit [start] [end]
     * 返回字符串里面第一个被设置为1或者0的bit位。
     * start 和 end 参数可以指定查找的范围。
     *
     * @param key   键名
     * @param bit   要查找的位值（0或1）
     * @param start 起始字节位置
     * @return 字符串第一个被设置为bit的bit位，如果没找到，返回-1
     */
    default Future<Long> bitPos(String key, int bit, long start) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(bit));
            args.add(String.valueOf(start));
            return api.bitpos(args).compose(response -> Future.succeededFuture(response.toLong()));
        });
    }

    /**
     * BITPOS key bit [start] [end]
     * 返回字符串里面第一个被设置为1或者0的bit位。
     * start 和 end 参数可以指定查找的范围。
     *
     * @param key   键名
     * @param bit   要查找的位值（0或1）
     * @param start 起始字节位置
     * @param end   结束字节位置
     * @return 字符串第一个被设置为bit的bit位，如果没找到，返回-1
     */
    default Future<Long> bitPos(String key, int bit, long start, long end) {
        return api(api -> {
            List<String> args = new ArrayList<>();
            args.add(key);
            args.add(String.valueOf(bit));
            args.add(String.valueOf(start));
            args.add(String.valueOf(end));
            return api.bitpos(args).compose(response -> Future.succeededFuture(response.toLong()));
        });
    }

    /**
     * GETBIT key offset
     * 返回位于 offset 处的bit值
     *
     * @param key    键名
     * @param offset 位偏移量
     * @return 字符串在 offset 处的bit，当 offset 超出了字符串长度的时候，这个字符串就被假定为由0比特填充的连续空间
     */
    default Future<Integer> getBit(String key, long offset) {
        return api(api -> api.getbit(key, String.valueOf(offset))
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }

    /**
     * SETBIT key offset value
     * 设置或者清除位于offset处的bit值
     *
     * @param key    键名
     * @param offset 位偏移量
     * @param value  bit值，只能是0或1
     * @return 原来位于 offset 处的bit值
     */
    default Future<Integer> setBit(String key, long offset, int value) {
        return api(api -> api.setbit(key, String.valueOf(offset), String.valueOf(value))
                             .compose(response -> Future.succeededFuture(response.toInteger())));
    }
}
