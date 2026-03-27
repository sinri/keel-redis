# API 速查（5.0.1）

下列按 **Mixin / 主题** 归纳 `RedisKit` 上的方法。完整签名与 Redis 语义见源码 JavaDoc。

## RedisApiMixin — 通用与键空间

| 主题          | 代表方法                                                                                                                     |
|-------------|--------------------------------------------------------------------------------------------------------------------------|
| 底层调度        | `api`, `withConnection`, `withTransaction`                                                                               |
| 键存在与删除      | `doesKeyExist`, `countExistedKeys`, `deleteKey(s)`, `unlinkKey(s)`                                                       |
| 类型与随机键      | `getValueTypeOfKey`, `randomKey`                                                                                         |
| 过期          | `expire`, `expireAt`, `expireInMillisecond`, `expireAtInMillisecond`, `persist`, `getTTLInSecond`, `getTTLInMillisecond` |
| 扫描与键枚举      | `keys`（慎用）, `scan`                                                                                                       |
| 改名与迁移       | `renameKey`, `renameKeyIfNewKeyNotExists`, `move`, `copy`, `migrate`, `dump`, `restore`                                  |
| 维护          | `touch`, `objectEncoding`, `objectRefcount`, `objectIdletime`, `objectFreq`                                              |
| 排序          | `sort`                                                                                                                   |
| 复制与等待       | `wait`（主从同步语义）                                                                                                           |
| 字符串扩展       | `getdel`, `getex`（含 `ExpireOption` 枚举）, `getset`（已弃用）                                                                    |
| 哈希/集合/有序集扫描 | `hscan`, `sscan`, `zscan`（返回 `HScanResult` / `SScanResult` / `ZScanResult`）                                              |
| 客户端与服务器     | `clientInfo`, `clientList`（含 `ClientType`）, `dbsize`, `flushdb`, `flushall`, `save`, `bgsave`                            |
| 嵌套类型        | `ValueType`, `ScanResult`, `FieldValuePair`, `MemberScorePair`, `ExpireOption`, `ClientType`                             |

## RedisScalarMixin — 字符串与数字

| 主题    | 代表方法                                                                                                                                     |
|-------|------------------------------------------------------------------------------------------------------------------------------------------|
| SET 族 | `setScalarToKeyForSeconds`, `setScalarToKeyForMilliseconds`, `setScalarToKeyForever`（均支持 `SetMode`：NX/XX）                                |
| GET 族 | `getString`, `getSubstring`, `getLCS`, `getLCSLength`                                                                                    |
| 计数    | `increment`（1 / long / double）, `decrement`（1 / long）                                                                                    |
| 批量    | `getMultipleStrings`, `setMultipleStrings`, `setMultipleStringsIfNotExist`                                                               |
| 其他    | `appendForKey`, `setStringIfKeyNotExists`, `setSubstring`, `getStringLength`, `setStringWithExpireTime`, `setStringWithExpireTimeMillis` |
| 已弃用   | `replaceString`（GETSET）, `getLongestCommonSubsequenceUsingStrAlgo`, `getLongestCommonSubsequenceWithIdxWithStrAlgo`（STRALGO，Redis 7 移除）  |

## RedisListMixin — 列表

| 主题    | 代表方法                                                                                                                                                                        |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 压入弹出  | `pushToListHead/Tail`, `pushToExistedListHead/Tail`, `popFromListHead/Tail`                                                                                                 |
| 长度与范围 | `getListLength`, `fetchListWithRange`, `trimList`                                                                                                                           |
| 查找与修改 | `getElementInList`, `setElementInList`, `insertIntoListBefore`, `seekElementInList`, `seekFirstElementInList`, `seekLastElementInList`, `removeSomeMatchedElementsFromList` |
| 移动    | `moveElementBetweenLists`（含 `ListDirection` 重载）, `blockingMoveElementBetweenLists`                                                                                          |
| 阻塞    | `blockingPopFromListsHead`, `blockingPopFromListsTail`                                                                                                                      |
| 已弃用   | `popTailAndPushHead`, `blockingPopTailAndPushHead` → 建议 `moveElementBetweenLists` / `blockingMoveElementBetweenLists`                                                       |

## RedisHashMixin — 哈希

| 主题    | 代表方法                                                                                                                    |
|-------|-------------------------------------------------------------------------------------------------------------------------|
| 读写    | `getHashField`, `setHashField`, `setHashFields`, `setHashFieldIfNotExists`, `getAllHashFields`, `getMultipleHashFields` |
| 删除与存在 | `deleteHashField`, `existsHashField`                                                                                    |
| 元数据   | `getHashLength`, `getHashKeys`, `getHashValues`, `getHashFieldValueLength`                                              |
| 数字    | `incrementHashField`, `incrementHashFieldByFloat`                                                                       |
| 已弃用   | `setMultipleHashFields`（HMSET）, `scanHash` → 使用 `RedisApiMixin.hscan`                                                   |

## RedisSetMixin — 集合

| 主题    | 代表方法                                                                                                                   |
|-------|------------------------------------------------------------------------------------------------------------------------|
| 成员    | `addToSet`, `removeFromSet`, `isSetMember`, `areSetMembers`, `getSetMembers`                                           |
| 基数与随机 | `getSetCardinality`, `getRandomSetMember(s)`, `popRandomSetMember(s)`, `moveSetMember`                                 |
| 集合运算  | `getSetDifference`, `storeSetDifference`, `getSetIntersection`, `storeSetIntersection`, `getSetUnion`, `storeSetUnion` |
| 迭代    | `scanSet`（`SScanResult`）                                                                                               |

## RedisOrderedSetMixin — 有序集合

| 主题    | 代表方法                                                                                                                                                          |
|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 写入与分数 | `addToOrderedSet`（含 options 映射）, `incrementOrderedSetMemberScore`, `getOrderedSetMemberScore`, `getMultipleMemberScores`                                      |
| 排名与范围 | `getOrderedSetMemberRank`, `getOrderedSetMemberReverseRank`, `getOrderedSetRange`, `countOrderedSetElementsInScoreRange`, `countOrderedSetElementsInLexRange` |
| 弹出    | `popMaxMembersFromOrderedSet`, `popMinMembersFromOrderedSet`, `blockingPopMaxFromOrderedSets`, `blockingPopMinFromOrderedSets`                                |
| 删除范围  | `removeFromOrderedSet`, `removeOrderedSetRangeByRank`, `removeOrderedSetRangeByScore`, `removeOrderedSetRangeByLex`                                           |
| 交并    | `intersectOrderedSets`, `storeIntersectionOfOrderedSets`, `unionOrderedSets`, `storeUnionOfOrderedSets`                                                       |
| 迭代    | `scanOrderedSet`（委托 `zscan`）                                                                                                                                  |
| 已弃用   | `getOrderedSetRangeByLex/Score`, `getOrderedSetReverseRange*`, `getOrderedSetReverseRangeByLex/Score`（4.1.0 起）                                                |

## RedisBitMixin — 位图

| 主题    | 代表方法                                                   |
|-------|--------------------------------------------------------|
| 计数与查找 | `bitCount`, `bitPos`                                   |
| 读写位   | `getBit`, `setBit`                                     |
| 位域    | `bitfieldGet`, `bitfieldSet`                           |
| 位运算   | `bitOp`, `bitOpNot`, `bitOpAnd`, `bitOpOr`, `bitOpXor` |

## 缓存包

| 类                         | 说明                                 |
|---------------------------|------------------------------------|
| `KeelAsyncCacheWithRedis` | 见 [async-cache.md](async-cache.md) |

## 查阅源码位置

包路径：`io.github.sinri.keel.integration.redis.kit`、`io.github.sinri.keel.integration.redis.cache`。
