package com.vcredit.framework.fmp.data.redis.lock;

import com.vcredit.framework.fmp.data.redis.config.RedisPlusAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>
 * DistributedLockTests
 * <p>
 *
 * @author: kancy
 * @date: 2020/3/7 14:13
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedisPlusAutoConfiguration.class, RedisAutoConfiguration.class})
@ActiveProfiles({"test"})
public class DistributedLockTests {

    @Autowired
    private RedisDistributedLock distributedLock;

    @Test
    public void test(){
        distributedLock.show();
    }
}
