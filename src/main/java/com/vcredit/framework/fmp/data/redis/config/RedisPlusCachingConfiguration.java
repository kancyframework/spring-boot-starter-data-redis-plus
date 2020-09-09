package com.vcredit.framework.fmp.data.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcredit.framework.fmp.data.redis.properties.RedisCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


/**
 * redis缓存配置
 * @author kancy
 */
@ConditionalOnClass({RedisSerializer.class, CacheManager.class})
@EnableCaching
public class RedisPlusCachingConfiguration {
    private static Logger log = LoggerFactory.getLogger(RedisPlusCachingConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConfigurationProperties("spring.redis.cache")
    public RedisCacheProperties redisCacheProperties(){
        return new RedisCacheProperties();
    }

    /**
     * 缓存管理器
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean({CacheManager.class})
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, RedisCacheProperties redisProperties) {
        String defaultKeyPrefix = getDefaultKeyPrefix(redisProperties);
        // 固定的时间缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = getFixedCacheConfigMap(redisProperties,defaultKeyPrefix);
        // 设置特定的缓存配置
        redisProperties.getTopics().entrySet().forEach(e ->{
            String cacheName = StringUtils.isEmpty(e.getValue().getCacheName()) ? e.getKey() : e.getValue().getCacheName();
            cacheConfigurations.put(cacheName, createDynamicRedisCacheConfiguration(cacheName, e.getValue(), defaultKeyPrefix));
        });
        RedisCacheManager redisCacheManager = RedisCacheManager.RedisCacheManagerBuilder
                .fromCacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .withInitialCacheConfigurations(cacheConfigurations)
                .cacheDefaults(initDefaultRedisCacheConfiguration(defaultKeyPrefix)).build();
        log.info("redisCacheManager 初始化完成！");
        return redisCacheManager;
    }

    /**
     * 获取固定延时时间的RedisCacheConfiguration
     * @return
     */
    private Map<String, RedisCacheConfiguration> getFixedCacheConfigMap(RedisCacheProperties redisProperties, String defaultKeyPrefix) {
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new LinkedHashMap<>();

        Set<String> dynamicTopicSet = new HashSet<>();
        dynamicTopicSet.addAll(redisProperties.getTopics().keySet());
        dynamicTopicSet.addAll(redisProperties.getTopics().values().stream()
                .map(RedisCacheProperties.Config::getCacheName)
                .collect(Collectors.toSet()));

        String[] ttlTopics = redisProperties.getTtlTopics();
        if (ttlTopics.length > 0){
            Set<String> ttlTopicSet = Arrays.stream(ttlTopics)
                    .filter(ttlTopic -> !dynamicTopicSet.contains(ttlTopic))
                    .collect(Collectors.toSet());
            ttlTopicSet.forEach(ttlTopic ->{
                RedisCacheConfiguration ttlTopicConfig = createFixedRedisCacheConfiguration(DurationStyle.detectAndParse(ttlTopic),
                        getFixedKeyPrefix(defaultKeyPrefix, ttlTopic));
                redisCacheConfigurationMap.put(ttlTopic, ttlTopicConfig);
            });
        }
        return redisCacheConfigurationMap;
    }

    /**
     * 创建固定延时时间的RedisCacheConfiguration
     * @param ttl
     * @param prefixKey
     * @return
     */
    private RedisCacheConfiguration createFixedRedisCacheConfiguration(Duration ttl, String prefixKey) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()))
                .disableCachingNullValues()
                .prefixKeysWith(prefixKey);
    }

    /**
     * 创建动态RedisCacheConfiguration
     * @param cacheName
     * @param config
     * @param defaultKeyPrefix
     * @return
     */
    private RedisCacheConfiguration createDynamicRedisCacheConfiguration(String cacheName, RedisCacheProperties.Config config,
                                                                         String defaultKeyPrefix) {
        // 默认配置
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()));
        // 有效期
        if (Objects.nonNull(config.getTtl())){
            cacheConfig = cacheConfig.entryTtl(config.getTtl());
        }
        // 是否缓存Null值
        if (!config.isCacheNullValues()){
            cacheConfig = cacheConfig.disableCachingNullValues();
        }
        // 是否使用key前缀
        if (!config.isUsePrefix()){
            cacheConfig = cacheConfig.disableKeyPrefix();
        } else {
            cacheConfig.prefixKeysWith(String.format("%s%s:", defaultKeyPrefix, cacheName));
        }
        return cacheConfig;
    }

    /**
     * 初始化默认的RedisCacheConfiguration
     * @param defaultKeyPrefix
     * @return
     */
    private RedisCacheConfiguration initDefaultRedisCacheConfiguration(String defaultKeyPrefix) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()));
        // key前缀
        defaultCacheConfig.prefixKeysWith(defaultKeyPrefix);
        return defaultCacheConfig;
    }

    /**
     * 序列化
     * @return
     */
    private RedisSerializer<Object> jsonSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    /**
     * 获取默认前缀
     * @param redisProperties
     * @return
     */
    private String getDefaultKeyPrefix(RedisCacheProperties redisProperties) {
        String project = redisProperties.getProject();
        String keyPrefix = redisProperties.getKeyPrefix();
        if(StringUtils.isEmpty(project)){
            project = applicationContext.getEnvironment().getProperty("spring.application.name",
                    CacheConstants.DEFAULT_PROJECT_NAME);
        }
        if(StringUtils.isEmpty(keyPrefix)){
            return String.format("%s:", project);
        }
        return String.format("%s:%s:", project, keyPrefix);
    }

    private String getFixedKeyPrefix(String defaultKeyPrefix, String name) {
        return String.format("%s%s:", defaultKeyPrefix, name);
    }
}
