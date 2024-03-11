package com.jxy.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 * @author 江喜原
 */
@SpringBootApplication
@MapperScan("com.jxy.usercenter.mapper")
@EnableScheduling // 开启定时任务
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

}

