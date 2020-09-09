package com.vcredit.framework.fmp.data.redis.lock;

import com.vcredit.framework.fmp.data.redis.config.RedisScriptConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * RedisDistributedLock
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 0:52
 **/
public class RedisDistributedLock implements DistributedLock {
    private Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String charsetName = "UTF-8";

    @Override
    public boolean lock(String key, String value) {
        Assert.hasText(key,"lock key is empty");
        Assert.hasText(value,"lock value is empty");
        RedisCallback<Boolean> callback = connection -> connection.setNX(key.getBytes(Charset.forName(charsetName)),
                value.getBytes(Charset.forName(charsetName)));
        try {
            return redisTemplate.execute(callback);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean lock(String key, String value, long expire) {
        return lock(key, value, expire, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean lock(String key, String value, long expire, TimeUnit timeUnit) {
        Assert.hasText(key,"lock key is empty");
        Assert.hasText(value,"lock value is empty");
        Assert.isTrue(expire >= 0,"expire must to be more than the 0");
        Assert.notNull(timeUnit,"timeUnit is null");
        RedisCallback<Boolean> callback = connection -> connection.set(key.getBytes(Charset.forName("UTF-8")),
                value.getBytes(Charset.forName("UTF-8")),
                Expiration.from(expire, timeUnit),
                RedisStringCommands.SetOption.SET_IF_ABSENT);
        try {
            return redisTemplate.execute(callback);
        } catch (Exception e) {
            logger.error("{}({}) unlock error: ",key, value, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean unlock(String key, String value) {
        Assert.hasText(key,"unlock key is empty");
        Assert.hasText(value,"unlock value is empty");
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            Object result = redisTemplate.execute(RedisScriptConfig.getUnlockRedisScript(), keys, value);
            return Objects.equals(String.valueOf(result), "1");
        } catch (Exception e) {
            logger.debug("{}({}) unlock error: ",key, value, e.getMessage());
            return false;
        }
    }

    @Override
    public String get(String key) {
        try {
            RedisCallback<String> callback = connection ->
                    new String(connection.get(key.getBytes()), Charset.forName(charsetName));
            return redisTemplate.execute(callback);
        } catch (Exception e) {
            logger.error("get redis lock key {} occurred an exception: {}",key, e);
        }
        return null;
    }


    /**
     * 分布式锁演示
     * @return
     */
    @Deprecated
    public void show() {
        final String lockKey = "redisDistributedLock-show-key";
        final int caseSize = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(caseSize);
        final List<Thread> list = new ArrayList(caseSize);
        for (int i = 0; i <caseSize; i++) {
            Thread thread = new Thread(() -> {
                String requestId = UUID.randomUUID().toString();
                try {
                    // 等等其他线程就绪
                    countDownLatch.await();
                    // 抢占锁
                    boolean tryLock = this.lock(lockKey, requestId, 100, TimeUnit.MILLISECONDS);
                    while (!tryLock){
                        // 没有抢占到锁时，休眠10ms
                        TimeUnit.MILLISECONDS.sleep(10);
                        tryLock = this.lock(lockKey, requestId, 100, TimeUnit.MILLISECONDS);
                    }
                    logger.info("线程[{}]获取分布式锁成功：{}",Thread.currentThread().getName() , requestId);
                } catch (Exception e) {
                    logger.error(lockKey, e);
                }finally {
                    // 释放锁
                    this.unlock(lockKey,requestId);
                    logger.info("线程[{}]释放分布式锁成功：{}",Thread.currentThread().getName() , requestId);
                }
            });
            thread.start();
            countDownLatch.countDown();
            list.add(thread);
        }

        // 等待演示完成
        Iterator<Thread> iterator = list.iterator();
        while (iterator.hasNext()){
            try {
                Thread thread = iterator.next();
                thread.join();
            } catch (InterruptedException e) {
                logger.error(lockKey, e);
            }finally {
                iterator.remove();
            }
        }
        list.clear();
    }
}
