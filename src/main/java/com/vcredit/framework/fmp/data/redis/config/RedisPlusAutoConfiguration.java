package com.vcredit.framework.fmp.data.redis.config;

import com.vcredit.framework.fmp.data.redis.lock.DistributedLock;
import com.vcredit.framework.fmp.data.redis.lock.RedisDistributedLock;
import com.vcredit.framework.fmp.data.redis.service.RedisService;
import com.vcredit.framework.fmp.data.redis.service.impl.RedisServiceImpl;
import com.vcredit.framework.fmp.data.redis.utils.RedisUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis配置类
 * @author kancy
 */
public class RedisPlusAutoConfiguration {

    /**
     * 自定义序列化类型RedisTemplate
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        return new StringRedisTemplate(redisConnectionFactory);
    }

    /**
     * 连接服务类RedisService
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisService redisService(StringRedisTemplate stringRedisTemplate) {
        RedisServiceImpl redisService = new RedisServiceImpl(stringRedisTemplate);
        RedisUtils.setRedisService(redisService);
        return redisService;
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLock distributedLock(){
        return new RedisDistributedLock();
    }
}