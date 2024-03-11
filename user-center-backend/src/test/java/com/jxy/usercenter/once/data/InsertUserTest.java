package com.jxy.usercenter.once.data;

import cn.hutool.core.date.StopWatch;
import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InsertUserTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService =  new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Test
    void insertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        User user = new User();
        for (int i = 0; i < 100000; i++) {
            user.setId((long) (i + 1));
            user.setUsername("江喜原-" + (i + 1));
            user.setUserAccount("江喜原-" + (i + 1));
            user.setAvatarUrl("https://himg.bdimg.com/sys/portraitn/item/public.1.e137c1ac.yS1WqOXfSWEasOYJ2-0pvQ");
            user.setGender(1);
            user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
            user.setPhone("15816709933");
            user.setEmail("1354768820@qq.com");
            user.setUserStatus(0);
            user.setUserRole(1);
            user.setPlanetCode(String.valueOf(i + 1));
            user.setTags("[\"java\", \"c++\"]");
            userService.save(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 批量插入用户
     */
    @Test
    void insertUserBatch() {
        final int INSERT_NUM = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ArrayList<User> userList = new ArrayList<>();
        for (int i = 1; i <= INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("江喜原-" + (i));
            user.setUserAccount("江喜原-" + (i));
            user.setAvatarUrl("https://himg.bdimg.com/sys/portraitn/item/public.1.e137c1ac.yS1WqOXfSWEasOYJ2-0pvQ");
            user.setGender(1);
            user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
            user.setPhone("15816709933");
            user.setEmail("1354768820@qq.com");
            user.setUserStatus(0);
            user.setUserRole(1);
            user.setPlanetCode(String.valueOf(i));
            user.setTags("[\"java\", \"c++\"]");
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        // 花费时间为：35502
        System.out.println("花费时间为：" + stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入用户
     */
    @Test
    void insertUserBatchFuture() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 分十组
        int batchSize = 10000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while(true) {
                j++;
                User user = new User();
                user.setUsername("假鱼皮");
                user.setUserAccount("fakeyupi");
                user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123");
                user.setEmail("123@qq.com");
                user.setTags("[\"女\"]");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("11111111");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " +Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        // 20 秒 10 万条
        stopWatch.stop();
        // 5828
        System.out.println(stopWatch.getTotalTimeMillis());

    }
}