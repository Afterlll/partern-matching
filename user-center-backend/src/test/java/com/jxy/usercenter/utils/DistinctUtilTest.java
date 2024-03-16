package com.jxy.usercenter.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jxy.usercenter.mapper.UserMapper;
import com.jxy.usercenter.model.domain.User;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DistinctUtilTest {

    @Resource
    private UserMapper userMapper;

    @Test
    void minDistance() {
        System.out.println(DistinctUtil.minDistance("abcd", "abc"));
    }
    @Test
    void priorityQueue() {
        List<String> s1 = Arrays.asList("大一", "python");
        List<String> s2 = Arrays.asList("大一");
        List<String> s3 = Arrays.asList("python");
        List<String> s4 = Arrays.asList("大一", "python", "java");
        List<String> s5 = Arrays.asList("大二", "python");
        List<String> s6 = Arrays.asList("java", "c++");
        System.out.println(DistinctUtil.minDistanceTags(s1, s2));
        System.out.println(DistinctUtil.minDistanceTags(s1, s3));
        System.out.println(DistinctUtil.minDistanceTags(s1, s4));
        System.out.println(DistinctUtil.minDistanceTags(s1, s5));
        System.out.println(DistinctUtil.minDistanceTags(s1, s6));
    }


    @Test
    public void test() {
        /*
        SELECT * FROM your_table
        WHERE id IN (1, 3, 2)
        ORDER BY FIELD(id, 1, 3, 2);
        使用以上SQL语句保证从数据库查出来的数据不被打乱
         */
        List<Long> ids = Arrays.asList(1L, 3L, 2L);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        String jsonStr = JSONUtil.toJsonStr(ids);
        queryWrapper.last("ORDER BY FIELD(id, " + jsonStr.substring(1, jsonStr.length() - 1) + ")");
        System.out.println("ORDER BY FIELD(id, " + jsonStr.substring(1, jsonStr.length() - 1) + ")");
//        User user = userMapper.selectById(11);
//        User user1 = userMapper.selectById(3);
//        int distance = DistinctUtil.minDistanceTags(JSONUtil.toBean(user.getTags(), new TypeReference<List<String>>() {}, false),
//                JSONUtil.toBean(user1.getTags(), new TypeReference<List<String>>() {}, false));
//        System.out.println(distance);
    }
}