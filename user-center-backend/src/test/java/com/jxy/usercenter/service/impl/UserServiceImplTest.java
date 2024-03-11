package com.jxy.usercenter.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxy.usercenter.controller.UserController;
import com.jxy.usercenter.model.domain.User;
import com.jxy.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserController userController;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void searchUserByTags() {
        List<String> list = Arrays.asList("java", "c++");
        System.out.println(userController.searchUserByTags(list));
    }

    @Test
    void setRecommendUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        IPage<User> userList = userService.page(new Page<>(1, 8), queryWrapper);
        List<User> list = userList.getRecords().stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        stringRedisTemplate.opsForValue().set("commend:1", JSONUtil.toJsonStr(list));
    }

    @Test
    void getRecommendUsers() {
        String redisValue = stringRedisTemplate.opsForValue().get("partner:matching:user:recommend:1");
        List<User> list = JSONUtil.toBean(redisValue, new TypeReference<List<User>>() {
        }, false);
        System.out.println(list);
    }
}