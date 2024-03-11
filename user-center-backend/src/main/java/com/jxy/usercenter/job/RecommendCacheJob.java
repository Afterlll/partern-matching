package com.jxy.usercenter.job;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户推荐缓存预热
 * 每天 1点 执行一次
 */
@Component
@Slf4j
public class RecommendCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private List<Long> hotUserList = new ArrayList<>(Arrays.asList(1L));

    // 秒 分 时 日 月 年（每天凌晨两点钟）
    // {秒数} {分钟} {小时} {日期} {月份} {星期} {年份(可为空)}
    @Scheduled(cron = "0 35 20 * * ?")
    public void recommendCacheJob() {
        RLock lock = redissonClient.getLock("partner:matching:job:lock");
        try {
            // 三个参数 等待时间、超时释放时间、时间单位
            if (lock.tryLock(0, -1, TimeUnit.MICROSECONDS)) {
                log.info("recommendCacheJob begin");
                for (Long userId : hotUserList) {
                    try {
                        String recommendKey = userService.getRecommendKey(userId);
                        // 1. 删除掉之前的缓存
                        stringRedisTemplate.delete(recommendKey);
                        // 2. 从数据库中查询出数据
                        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                        IPage<User> userList = userService.page(new Page<>(1, 8), queryWrapper);
                        List<User> list = userList.getRecords().stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                        // 3. 更新redis缓存
                        stringRedisTemplate.opsForValue().set(recommendKey, JSONUtil.toJsonStr(list), 1, TimeUnit.DAYS);
                    } catch (Exception e) {
                        log.error("recommendCacheJob error", e);
                    }
                }
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
