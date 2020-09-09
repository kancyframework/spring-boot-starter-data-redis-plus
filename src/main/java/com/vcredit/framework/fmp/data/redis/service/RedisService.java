package com.vcredit.framework.fmp.data.redis.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * RedisService
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 0:42
 **/

public interface RedisService {
    /**
     * 将字符串值 value 关联到 key
     */
    boolean set(String key, Object value);
    /**
     * 将字符串值 value 关联到 key，并设置过期时间，单位秒
     */
    boolean set(String key, Object value, long seconds);
    /**
     * 将字符串值 value 关联到 key，并设置过期时间
     */
    boolean set(String key, Object value, Duration duration);
    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     */
    boolean setnx(String key, Object value);

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     */
    boolean setnx(String key, Object value, long seconds);

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     */
    boolean setnx(String key, Object value, Duration duration);

    /**
     * 返回 key 所关联的字符串值。
     */
    Optional<String> get(String key);

    /**
     * 返回 key 所关联的类型。
     */
    <T> Optional<T> get(String key, Class<T> classType);

    /**
     * 设置过期时间
     */
    boolean expire(String key, long expire);

    /**
     * 返回key值所对应的对象的list
     */
    <T> List<T> getList(String key, Class<T> clz);

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     */
    long lpush(String key, Object obj);

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾
     */
    long rpush(String key, Object obj);

    /**
     * 移除并返回列表 key 的头元素。
     */
    Optional<String> lpop(String key);

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     */
    boolean hmset(String key, Map<String, Object> hashMap);

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。
     */
    List<String> hmget(String key);

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。
     */
    boolean hset(String key, String hashKey, Object value);

    /**
     * 返回哈希表 key 中给定域 field 的值。
     */
    Optional<String> hget(String key, String hashKey);

    /**
     * redis 删除hashkey
     */
    long hdel(String key, String hashKeys);


    /**
     * 查找所有符合给定模式 pattern 的 key 。
     * KEYS * 匹配数据库中所有 key 。
     * KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
     * KEYS h*llo 匹配 hllo 和 heeeeello 等。
     * KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
     * 特殊符号用 \ 隔开
     */
    Set<String> keys(String pattern);

    /**
     * 删除给定的key 。
     */
    boolean del(String key);

    /**
     * 判断是否存在key 。
     */
    boolean exists(String key);

    /**
     * redis zadd
     * 实现延时队列
     * 拿时间戳作为score
     */
    boolean zadd(String key, double score, Object value);

    /**
     * redis zadd
     */
    boolean zadd(String key, List<Object> values);

    /**
     * zRemove
     * @param key
     * @param members
     * @return
     */
    boolean zRemove(String key, String... members);

    /**
     * removeRangeByScore
     * @param key
     * @param start
     * @param end
     * @return
     */
    boolean removeRangeByScore(String key, double start, double end);

    /**
     * redis zrange 所有元素
     */
    Set<String> zrangeAll(String key);

    /**
     * redis zrange 指定元素
     */
    Set<String> zrange(String key, long start, long end);


    /**
     * 功能描述： 减1操作,并设置过期时间，并事务乐观锁
     */
    boolean decrAndExpireByLock(String key, int seconds);

    /**
     * 功能描述： 减1操作,并在减为0时设置过期时间，并事务乐观锁
     */
    boolean decrAndLastExpireByLock(String key, int seconds);

    /**
     * 功能描述： 库存操作，超时时间只在初始化时设置
     *
     * @param key       key
     * @param initStock 初始化库存
     * @param seconds   超时时间，初始化时配置，传0则意味着不过期
     *
     * @return
     */
    boolean stock(String key, int initStock, int seconds);

    /**
     * 获取过期时间
     *
     * @param key
     *
     * @return
     */
    long expireTime(String key);

    /**
     * 获取过期时间
     *
     * @param key
     * @param timeUnit
     *
     * @return
     */
    long expireTime(String key, TimeUnit timeUnit);
}
