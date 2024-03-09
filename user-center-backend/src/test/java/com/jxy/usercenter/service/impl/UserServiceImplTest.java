package com.jxy.usercenter.service.impl;

import com.jxy.usercenter.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserController userController;

    @Test
    void searchUserByTags() {
        List<String> list = Arrays.asList("java", "c++");
        System.out.println(userController.searchUserByTags(list));
    }
}