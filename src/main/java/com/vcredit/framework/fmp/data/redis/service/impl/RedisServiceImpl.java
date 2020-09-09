package com.vcredit.framework.fmp.data.redis.service.impl;

import com.vcredit.framework.fmp.data.redis.config.RedisScriptConfig;
import com.vcredit.framework.fmp.data.redis.service.RedisService;
import net.dreamlu.mica.core.utils.$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * RedisServiceImpl
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 0:52
 **/

public class RedisServiceImpl implements RedisService {
    private static Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final StringRedisTemplate redisTemplate;

    private final RedisSerializer<String> serializer = new StringRedisSerializer();

    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String toJsonString(Object value) {
        if (value instanceof String){
            return (String)value;
        }
        return $.toJson(value);
    }

    private <T> T parseJsonString(String jsonStr, Class<T> classType) {
        return $.readJson(jsonStr, classType);
    }

    private <T> List<T> parseJsonArray(String jsonArrayStr, Class<T> classType) {
        return $.readJsonAsList(jsonArrayStr, classType);
    }

    private void handleException(Exception e) {
        log.warn(e.getMessage());
    }

    /**
     * 将字符串值 value 关联到 key
     *
     * @param key
     * @param value
     */
    @Override
    public boolean set(String key, Object value) {
        try{
            return redisTemplate.execute((RedisCallback<Boolean>)
                    connection -> connection.set(serializer.serialize(key), serializer.serialize(toJsonString(value))));
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 将字符串值 value 关联到 key，并设置过期时间，单位秒
     *
     * @param key
     * @param value
     * @param seconds
     */
    @Override
    public boolean set(String key, Object value, long seconds) {
        return set(key, value, Duration.ofSeconds(seconds));
    }

    /**
     * 将字符串值 value 关联到 key，并设置过期时间
     *
     * @param key
     * @param value
     * @param duration
     */
    @Override
    public boolean set(String key, Object value, Duration duration) {
        try{
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                try{
                    connection.pSetEx(serializer.serialize(key), duration.toMillis(),
                            serializer.serialize(toJsonString(value)));
                }catch(Exception ex){
                    return false;
                }
                return true;
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     *
     * @param key
     * @param value
     */
    @Override
    public boolean setnx(String key, Object value) {
        try{
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                Boolean result = connection.setNX(serializer.serialize(key),
                        serializer.serialize(toJsonString(value)));
                return result;
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     *
     * @param key
     * @param value
     * @param seconds
     */
    @Override
    public boolean setnx(String key, Object value, long seconds) {
        return setnx(key, value, Duration.ofSeconds(seconds));
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     *
     * @param key
     * @param value
     * @param duration
     */
    @Override
    public boolean setnx(String key, Object value, Duration duration) {
        try{
            RedisCallback<Boolean> callback =
                    connection -> connection.set(serializer.serialize(key), serializer.serialize(toJsonString(value)),
                            Expiration.milliseconds(duration.toMillis()), RedisStringCommands.SetOption.SET_IF_ABSENT);
            return redisTemplate.execute(callback);
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 返回 key 所关联的字符串值。
     *
     * @param key
     */
    @Override
    public Optional<String> get(String key) {
        try{
            String result = redisTemplate.execute((RedisCallback<String>)
                    connection -> serializer.deserialize(connection.get(serializer.serialize(key))));
            return Optional.ofNullable(result);
        }catch(Exception e){
            handleException(e);
            return Optional.empty();
        }
    }

    /**
     * 返回 key 所关联的类型。
     *
     * @param key
     * @param classType
     */
    @Override
    public <T> Optional<T> get(String key, Class<T> classType) {
        Optional<String> optional = get(key);
        try {
            if (optional.isPresent()){
                return Optional.ofNullable(parseJsonString(optional.get(), classType));
            }
        } catch (Exception e) {
            handleException(e);
            return Optional.empty();
        }
        return Optional.empty();
    }


    /**
     * 设置过期时间
     *
     * @param key
     * @param expire
     */
    @Override
    public boolean expire(String key, long expire) {
        try{
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                Boolean result = connection.expire(serializer.serialize(key), expire);
                return result;
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 设置key值所对应的对象的list
     * @param key
     * @param list
     * @return
     */
    @Override
    public boolean setList(String key, List<Object> list) {
        if (!CollectionUtils.isEmpty(list)){
            return set(key, list);
        }
        return false;
    }

    /**
     * 返回key值所对应的对象的list
     *
     * @param key
     * @param clz
     */
    @Override
    public <T> List<T> getList(String key, Class<T> clz) {
        Optional<String> json = get(key);
        if(json.isPresent()){
            return parseJsonArray(json.get(), clz);
        }
        return Collections.emptyList();
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     *
     * @param key
     * @param obj
     */
    @Override
    public long lpush(String key, Object obj) {
        try{
            long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                long count = connection.lPush(serializer.serialize(key), serializer.serialize(toJsonString(obj)));
                return count;
            });
            return result;
        }catch(Exception e){
            handleException(e);
            return 0L;
        }
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾
     *
     * @param key
     * @param obj
     */
    @Override
    public long rpush(String key, Object obj) {
        try{
            long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                long count = connection.rPush(serializer.serialize(key), serializer.serialize(toJsonString(obj)));
                return count;
            });
            return result;
        }catch(Exception e){
            handleException(e);
            return 0L;
        }
    }

    /**
     * 移除并返回列表 key 的头元素。
     *
     * @param key
     */
    @Override
    public Optional<String> lpop(String key) {
        try{
            String result = redisTemplate.execute((RedisCallback<String>)
                    connection -> serializer.deserialize(connection.lPop(serializer.serialize(key))));
            return Optional.ofNullable(result);
        }catch(Exception e){
            handleException(e);
            return Optional.empty();
        }
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     *
     * @param key
     * @param hashMap
     */
    @Override
    public boolean hmset(String key, Map<String, Object> hashMap) {
        try{

            Map<String, String> stringMap = new HashMap<>();
            hashMap.entrySet().stream().forEach(e -> stringMap.put(e.getKey(), toJsonString(e.getValue())));

            Boolean result = redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException {
                    BoundHashOperations<String, String, String> hv = ((RedisTemplate) operations).boundHashOps(key);
                    operations.multi();
                    hv.putAll(stringMap);
                    List<Object> result = operations.exec();
                    return !CollectionUtils.isEmpty(result) ? true : false;
                }
            });
            return result;
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。
     *
     * @param key
     */
    @Override
    public List<String> hmget(String key) {
        try{
            return redisTemplate.execute(new SessionCallback<List<String>>() {
                @Override
                public <K, V> List<String> execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundHashOperations<String, String, String> hv = ((RedisTemplate) operations).boundHashOps(key);
                    Set<String> set = hv.keys();
                    if(!CollectionUtils.isEmpty(set)){
                        return hv.multiGet(set);
                    }
                    return Collections.emptyList();
                }
            });
        }catch(Exception e){
            handleException(e);
            return Collections.emptyList();
        }
    }

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。
     *
     * @param key
     * @param hashKey
     * @param value
     */
    @Override
    public boolean hset(String key, String hashKey, Object value) {
        try{
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundHashOperations<String, String, String> hv = ((RedisTemplate) operations).boundHashOps(key);
                    operations.multi();
                    hv.put(hashKey, toJsonString(value));
                    List<Object> result = operations.exec();
                    return !CollectionUtils.isEmpty(result) ? true : false;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。
     *
     * @param key
     * @param hashKey
     */
    @Override
    public Optional<String> hget(String key, String hashKey) {
        try{
            String result = redisTemplate.execute(new SessionCallback<String>() {
                @Override
                public <K, V> String execute(RedisOperations<K, V> operations) throws DataAccessException {
                    BoundHashOperations<String, String, String> hv = ((RedisTemplate) operations).boundHashOps(key);
                    return hv.get(hashKey);
                }
            });
            return Optional.ofNullable(result);
        }catch(Exception e){
            handleException(e);
            return Optional.empty();
        }
    }

    /**
     * redis 删除hashkey
     *
     * @param key
     * @param hashKeys
     */
    @Override
    public long hdel(String key, String hashKeys) {
        try{
            return redisTemplate.execute(new SessionCallback<Long>() {
                @Override
                public <K, V> Long execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundHashOperations<String, String, String> hv = ((RedisTemplate) operations).boundHashOps(key);
                    return hv.delete(hashKeys);
                }
            });
        }catch(Exception e){
            handleException(e);
            return 0L;
        }
    }

    /**
     * 查找所有符合给定模式 pattern 的 key 。
     * KEYS * 匹配数据库中所有 key 。
     * KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
     * KEYS h*llo 匹配 hllo 和 heeeeello 等。
     * KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
     * 特殊符号用 \ 隔开
     *
     * @param pattern
     */
    @Override
    public Set<String> keys(String pattern) {
        try{
            return redisTemplate.execute(new SessionCallback<Set>() {
                @Override
                public <K, V> Set execute(RedisOperations<K, V> operations) throws DataAccessException{
                    Set<String> keySet = ((StringRedisTemplate) operations).keys(pattern);
                    return keySet;
                }
            });
        }catch(Exception e){
            handleException(e);
            return Collections.emptySet();
        }
    }

    /**
     * 删除给定的key 。
     *
     * @param key
     */
    @Override
    public boolean del(String key) {
        try{
            boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                try{
                    connection.del(serializer.serialize(key));
                }catch(Exception ex){
                    handleException(ex);
                }
                return true;
            });
            return result;
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 判断是否存在key 。
     *
     * @param key
     */
    @Override
    public boolean exists(String key) {
        try{
            return redisTemplate.execute((RedisCallback<Boolean>)
                    connection -> connection.exists(serializer.serialize(key)));
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /*
     * @param key
     * @param values
     * */
    @Override
    public boolean sadd(String key, Object ... values){
        try{
            Assert.notEmpty(values, "sadd values isEmpty");
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundSetOperations<String, String> bz = ((RedisTemplate) operations).boundSetOps(key);
                    for(int i = 0; i < values.length; i++){
                        bz.add(toJsonString(values[i]));
                    }
                    return true;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * redis zadd
     * 实现延时队列
     * 拿时间戳作为score
     *
     * @param key
     * @param score
     * @param value
     */
    @Override
    public boolean zadd(String key, double score, Object value) {
        try{
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundZSetOperations<String, String> bz = ((RedisTemplate) operations).boundZSetOps(key);
                    return bz.add(toJsonString(value), score);
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * redis zadd
     *
     * @param key
     * @param values
     */
    @Override
    public boolean zadd(String key, List<Object> values) {
        try{
            Assert.notEmpty(values, "zadd values isEmpty");
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundZSetOperations<String, String> bz = ((RedisTemplate) operations).boundZSetOps(key);
                    for(int i = 0; i < values.size(); i++){
                        bz.add(toJsonString(values.get(i)), Double.valueOf(i + 1));
                    }
                    return true;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * zRemove
     *
     * @param key
     * @param members
     * @return
     */
    @Override
    public boolean zRemove(String key, String ... members) {
        try{
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    ((RedisTemplate) operations).boundZSetOps(key).remove(members);
                    return true;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * removeRangeByScore
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public boolean removeRangeByScore(String key, double start, double end) {
        try{
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundZSetOperations<String, String> bz = ((RedisTemplate) operations).boundZSetOps(key);
                    // 移除原先的
                    bz.removeRangeByScore(start, end);
                    return true;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * redis zrange 所有元素
     *
     * @param key
     */
    @Override
    public Set<String> zrangeAll(String key) {
        return this.zrange(key, 0L, -1L);
    }

    /**
     * redis zrange 指定元素
     *
     * @param key
     * @param start
     * @param end
     */
    @Override
    public Set<String> zrange(String key, long start, long end) {
        return redisTemplate.execute(new SessionCallback<Set<String>>() {
            @Override
            public <K, V> Set<String> execute(RedisOperations<K, V> operations) throws DataAccessException{
                BoundZSetOperations<String, String> bound = ((RedisTemplate) operations).boundZSetOps(key);
                return bound.range(start, end);
            }
        });
    }

    /**
     * 功能描述： 减1操作,并设置过期时间，并事务乐观锁
     *
     * @param key
     * @param seconds
     */
    @Override
    public boolean decrAndExpireByLock(String key, int seconds) {
        try{
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundValueOperations<String, String> bv = ((RedisTemplate) operations).boundValueOps(key);
                    String stock = bv.get();
                    if(!StringUtils.isEmpty(stock)){
                        operations.multi();
                        int iStock = Integer.valueOf(stock);
                        if(iStock > 0){
                            bv.increment(-1);
                            bv.expire(seconds, TimeUnit.SECONDS);
                        }
                        List<Object> result = operations.exec();
                        return !CollectionUtils.isEmpty(result) ? true : false;
                    }
                    return true;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 功能描述： 减1操作,并在减为0时设置过期时间，并事务乐观锁
     *
     * @param key
     * @param seconds
     */
    @Override
    public boolean decrAndLastExpireByLock(String key, int seconds) {
        try{
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public <K, V> Boolean execute(RedisOperations<K, V> operations) throws DataAccessException{
                    BoundValueOperations<String, String> bv = ((RedisTemplate) operations).boundValueOps(key);
                    String stock = bv.get();
                    if(!StringUtils.isEmpty(stock)){
                        operations.multi();
                        int iStock = Integer.valueOf(stock);
                        if(iStock > 0){
                            bv.increment(-1);
                            if(iStock == 1){
                                bv.expire(seconds, TimeUnit.SECONDS);
                            }
                        }
                        List<Object> result = operations.exec();
                        return !CollectionUtils.isEmpty(result) ? true : false;
                    }
                    return true;
                }
            });
        }catch(Exception e){
            handleException(e);
            return false;
        }
    }

    /**
     * 功能描述： 库存操作，超时时间只在初始化时设置
     *
     * @param key       key
     * @param initStock 初始化库存
     * @param seconds   超时时间，初始化时配置，传0则意味着不过期
     * @return
     */
    @Override
    public boolean stock(String key, int initStock, int seconds) {
        if(initStock <= 0){
            return false;
        }
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            Object result = redisTemplate.execute(RedisScriptConfig.getStockRedisScript(), keys,
                    String.valueOf(initStock - 1), String.valueOf(seconds));
            return Objects.equals(String.valueOf(result), "1");
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key
     * @return
     */
    @Override
    public long getExpireTime(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 获取过期时间
     *
     * @param key
     * @param timeUnit
     * @return
     */
    @Override
    public long getExpireTime(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }
}
