package com.jxy.usercenter.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void redissonTest() {
        RList<Object> rList = redissonClient.getList("redisson-test");
//        rList.add("redisson-test");
        rList.remove("redisson-test");
    }

    @Test
    public void testWatchDog() {
        RLock lock = redissonClient.getLock("partner:matching:job:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MICROSECONDS)) {
                log.info("recommendCacheJob begin");
                Thread.sleep(100000);
                log.info("recommendCacheJob end");
            }
        } catch (InterruptedException e) {
            log.error("recommendCacheJob redisson error", e);
            throw new RuntimeException(e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
