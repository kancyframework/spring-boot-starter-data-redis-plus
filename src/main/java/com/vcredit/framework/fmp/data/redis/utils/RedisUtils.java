package com.vcredit.framework.fmp.data.redis.utils;

import com.vcredit.framework.fmp.data.redis.service.RedisService;
import com.vcredit.framework.fmp.data.redis.service.impl.RedisServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * REDIS连接工具类
 *
 * @author kancy
 */
public class RedisUtils {

    private static RedisService redisService;

    private static StringRedisTemplate redisTemplate;

    public static StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public static void setRedisTemplate(StringRedisTemplate redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
        RedisUtils.redisService = new RedisServiceImpl(redisTemplate);
    }

    /**
     * 将字符串值 value 关联到 key
     */
    public static boolean set(String key, String value){
        return redisService.set(key, value);
    }

    /**
     * 将字符串值 value 关联到 key，并设置过期时间，单位秒
     */
    public static boolean set(String key, String value, long seconds){
        return redisService.set(key, value, seconds);
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。 若给定的 key 已经存在，则 SETNX 不做任何动作。
     */
    public static boolean setnx(String key, String value){
        return redisService.setnx(key, value);
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。 若给定的 key 已经存在，则 SETNX 不做任何动作。
     */
    public static boolean setnx(String key, String value, long seconds){
        return redisService.setnx(key, value, seconds);
    }

    /**
     * 返回 key 所关联的字符串值。
     */
    public static Optional<String> get(String key){
        return redisService.get(key);
    }

    /**
     * 返回 key 所关联的类型。
     * @param key
     * @param classType
     * @return
     */
    public static <T> Optional<T> get(String key, Class<T> classType) {
        return redisService.get(key, classType);
    }

    /**
     * 设置过期时间
     */
    public static boolean expire(String key, long expire){
        return redisService.expire(key, expire);
    }

    /**
     * 将list以json的形式关联到key
     */
    public static boolean setList(String key, List<Object> list){
        return redisService.setList(key, list);
    }

    /**
     * 返回key值所对应的对象的list
     */
    public static <T> List<T> getList(String key, Class<T> clz){
        return redisService.getList(key, clz);
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     */
    public static boolean hmset(String key, Map<String, Object> hashMap){
        return redisService.hmset(key, hashMap);
    }

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。
     */
    public static List<String> hmget(String key){
        return redisService.hmget(key);
    }

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。
     */
    public static boolean hset(String key, String hashKey, String value){
        return redisService.hset(key, hashKey, value);
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。
     */
    public static Optional<String> hget(String key, String hashKey){
        return redisService.hget(key, hashKey);
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     */
    public static long lpush(String key, Object obj){
        return redisService.lpush(key, obj);
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾
     */
    public static long rpush(String key, Object obj){
        return redisService.rpush(key, obj);
    }

    /**
     * 移除并返回列表 key 的头元素。
     */
    public static Optional<String> lpop(String key){
        return redisService.lpop(key);
    }
    /**
     * BRPOP 是列表的阻塞式(blocking)弹出原语。
     * 弹出第一个非空列表的尾部元素。
     * @param timeout seconds to block.
     * @param key
     * @return
     */
    public static List<String> brpop(int timeout, String key){
        return redisService.brpop(timeout, key);
    }
    /**
     * BLPOP 是列表的阻塞式(blocking)弹出原语。
     * 弹出第一个非空列表的头元素。
     * @param timeout seconds to block.
     * @param key
     * @return
     */
    public static List<String> blpop(int timeout, String key){
        return redisService.blpop(timeout, key);
    }

    /**
     * redis sadd
     * @param key
     * @param values
     * @return
     */
    public static boolean sadd(String key, Object... values){
        return redisService.sadd(key, values);
    }

    /**
     * redis sadd
     * @param key
     * @param values
     * @return
     */
    public static boolean sadd(String key, List<Object> values){
        if (CollectionUtils.isEmpty(values)){
            return false;
        }
        return redisService.sadd(key, values.toArray());
    }

    /**
     * redis zadd
     */
    public static boolean zadd(String key, Object ... values){
        return redisService.zadd(key, Arrays.asList(values));
    }

    /**
     * redis zadd
     */
    public static boolean zadd(String key, List<Object> values){
        return redisService.zadd(key, values);
    }

    /**
     * redis zrange 所有元素
     */
    public static Set<String> zrangeAll(String key){
        return redisService.zrangeAll(key);
    }


    /**
     * redis  zset remove
     */
    public static boolean zRemove(String key, String... members){
        return redisService.zRemove(key, members);
    }

    /**
     * redis zrange 指定元素
     */
    public static Set<String> zrange(String key, long start, long end){
        return redisService.zrange(key, start, end);
    }

    /**
     * redis zadd
     */
    public static boolean zadd(String key, double score, String value){
        return redisService.zadd(key, score, value);
    }

    /**
     * zRemove
     * @param key
     * @param score
     * @param value
     * @return
     */
    public static boolean zRemove(String key, double score, String value){
        return redisService.zadd(key, score, value);
    }

    /**
     * removeRangeByScore
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static boolean removeRangeByScore(String key, double start, double end){
        return redisService.removeRangeByScore(key, start, end);
    }

    /**
     * 获取过期时间
     *
     * @param key
     *
     * @return
     */
    public static long getExpireTime(String key){
        return redisService.getExpireTime(key);
    }

    /**
     * 获取过期时间
     *
     * @param key
     * @param timeUnit
     *
     * @return
     */
    public static long getExpireTime(String key, TimeUnit timeUnit){
        return redisService.getExpireTime(key, timeUnit);
    }

    /**
     * 查找所有符合给定模式 pattern 的 key 。 KEYS * 匹配数据库中所有 key 。 KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。 KEYS h*llo 匹配 hllo 和
     * heeeeello 等。 KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。 特殊符号用 \ 隔开
     */
    public static Set<String> keys(String pattern){
        return redisService.keys(pattern);
    }

    /**
     * 删除给定的key 。
     */
    public static boolean del(String key){
        return redisService.del(key);
    }

    /**
     * 判断是否存在key 。
     */
    public static boolean exists(String key){
        return redisService.exists(key);
    }

    /**
     * 功能描述： 减1操作,并设置过期时间，并事务乐观锁
     */
    public static boolean decrAndExpireByLock(String key, int seconds){
        return redisService.decrAndExpireByLock(key, seconds);
    }

    /**
     * 功能描述： 减1操作,并在减为0时设置过期时间，并事务乐观锁
     */
    public static boolean decrAndLastExpireByLock(String key, int seconds){
        return redisService.decrAndLastExpireByLock(key, seconds);
    }

    /**
     * 功能描述： 库存操作，超时时间只在初始化时设置
     *
     * @param key       key
     * @param initStock 初始化库存
     * @param seconds   超时时间，初始化时配置，传0则意味着不过期
     *
     * @return
     */
    public static boolean stock(String key, int initStock, int seconds){
        return redisService.stock(key, initStock, seconds);
    }

}
