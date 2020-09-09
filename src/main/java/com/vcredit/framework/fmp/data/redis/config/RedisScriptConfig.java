package com.vcredit.framework.fmp.data.redis.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * <p>
 * RedisScriptConfig
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/5 12:07
 **/

public class RedisScriptConfig {

    private static RedisScript stockRedisScript;
    private static RedisScript unlockRedisScript;

    static {
        initStockRedisScript();
        initUnlockRedisScript();
    }

    private RedisScriptConfig() {
    }

    private static void initStockRedisScript() {
        DefaultRedisScript defaultRedisScript =new DefaultRedisScript<Long>();
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/redis_stock.lua")));
        defaultRedisScript.setResultType(Long.class);
        setStockRedisScript(defaultRedisScript);

    }

    private static void initUnlockRedisScript() {
        DefaultRedisScript defaultRedisScript =new DefaultRedisScript<List>();
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/redis_unlock.lua")));
        defaultRedisScript.setResultType(Long.class);
        setUnlockRedisScript(defaultRedisScript);
    }

    /**
     * 获取延时消息获取并且移除的脚本
     * @return
     */
    public static RedisScript getStockRedisScript() {
        return stockRedisScript;
    }

    /**
     * 获取延时消息保存的脚本
     * @return
     */
    public static RedisScript getUnlockRedisScript() {
        return unlockRedisScript;
    }

    private static void setStockRedisScript(RedisScript stockRedisScript) {
        RedisScriptConfig.stockRedisScript = stockRedisScript;
    }

    private static void setUnlockRedisScript(RedisScript unlockRedisScript) {
        RedisScriptConfig.unlockRedisScript = unlockRedisScript;
    }
}
