package com.vcredit.framework.fmp.data.redis.service;

import com.vcredit.framework.fmp.data.redis.config.RedisPlusAutoConfiguration;
import com.vcredit.framework.fmp.data.redis.config.RedisPlusCachingConfiguration;
import com.vcredit.framework.fmp.data.redis.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * RedisServiceTests
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 14:13
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedisPlusAutoConfiguration.class, RedisAutoConfiguration.class, RedisPlusCachingConfiguration.class})
@ActiveProfiles({"test"})
public class RedisServiceTests {

    @Autowired
    private RedisService redisService;

    @Test
    public void setTest() throws Exception {
        String key = "test:a";
        Assert.assertTrue(redisService.set(key, 1));
        Assert.assertTrue(redisService.get(key).isPresent());
        Assert.assertEquals("1", redisService.get(key).get());
        Assert.assertTrue(redisService.del(key));

        Assert.assertTrue(redisService.set(key, 1, Duration.ofMillis(10)));
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertFalse(redisService.get(key).isPresent());


        User user = new User("tom", 26);
        Assert.assertTrue(redisService.set(key, user));

        Optional<String> stringOptional = redisService.get(key);
        Assert.assertTrue(stringOptional.isPresent());
        System.out.println(stringOptional.get());
        Assert.assertEquals(new User("tom", 26),
                redisService.get(key, User.class).get());

        List users = new ArrayList<>();
        users.add(user);
        Assert.assertTrue(redisService.set(key, users));
        Assert.assertEquals(users, redisService.getList(key, User.class));

    }
}
