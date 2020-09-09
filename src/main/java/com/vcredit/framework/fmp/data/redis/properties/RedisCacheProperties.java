package com.vcredit.framework.fmp.data.redis.properties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.vcredit.framework.fmp.data.redis.config.CacheConstants.*;

/**
 * <p>
 * RedisCacheProperties
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 12:25
 **/
public class RedisCacheProperties {

    /**
     * 命名空间
     */
    private String project;

    /**
     * 前缀 = ${project}'keyPrefix'
     */
    private String keyPrefix;

    /**
     * 缓存配置
     */
    private Map<String, Config> topics = new HashMap<>();

    /**
     * 可过期的Topics
     */
    private String[] ttlTopics = new String[]{KEY_10M,KEY_30M,KEY_8H,KEY_24H,KEY_1D,KEY_30D};

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Map<String, Config> getTopics() {
        return topics;
    }

    public void setTopics(Map<String, Config> topics) {
        this.topics = topics;
    }

    public String[] getTtlTopics() {
        return ttlTopics;
    }

    public void setTtlTopics(String[] ttlTopics) {
        this.ttlTopics = ttlTopics;
    }

    /**
     * 配置
     */
    public static class Config {
        /**
         * 缓存名称
         */
        private String cacheName;
        /**
         * 延时时间
         */
        private Duration ttl;
        /**
         * 是否缓存null
         */
        private boolean cacheNullValues = Boolean.TRUE;
        /**
         * 使用前缀
         */
        private boolean usePrefix = Boolean.TRUE;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public boolean isCacheNullValues() {
            return cacheNullValues;
        }

        public void setCacheNullValues(boolean cacheNullValues) {
            this.cacheNullValues = cacheNullValues;
        }

        public boolean isUsePrefix() {
            return usePrefix;
        }

        public void setUsePrefix(boolean usePrefix) {
            this.usePrefix = usePrefix;
        }

        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }
    }

}
