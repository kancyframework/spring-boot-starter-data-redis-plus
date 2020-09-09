package com.vcredit.framework.fmp.data.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * DistributedLock
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 0:52
 **/
public interface DistributedLock {

    /**
     * 获取锁
     * @param key
     * @param value
     * @return
     */
    boolean lock(String key, String value);

    /**
     * 获取锁
     * @param key
     * @param value
     * @param expire
     * @return
     */
    boolean lock(String key, String value, long expire);

    /**
     * 获取锁
     * @param key
     * @param value
     * @param expire
     * @param timeUnit
     * @return
     */
    boolean lock(String key, String value, long expire, TimeUnit timeUnit);

    /**
     * 释放锁
     * @param key
     * @param value
     * @return
     */
    boolean unlock(String key, String value);

    /**
     * 获取锁内容
     * @param key
     * @return
     */
    String get(String key);
}
