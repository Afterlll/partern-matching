package com.jxy.usercenter.once.data;

import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * 插入用户数据（每次10w）
 */
@Component
public class InsertUser {

    @Resource
    private UserService userService;

    // 只执行一次
//    @Scheduled(fixedDelay = Long.MAX_VALUE) // 用一个非常大的延迟值，确保只执行一次
    public void insertUser() {
        User user = new User();
        ArrayList<User> userList = new ArrayList<>();
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
            userList.add(user);
            userService.save(user);
        }

//        userService.saveBatch(userList, 1000);
    }

}
