package com.jxy.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.redis")
@Data
public class RedissonConfig {

    private String host;
    private String port;

    @Bean
    public RedissonClient getRedissonClient() {
        // 1. Redisson 配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%s", host, port))
                .setDatabase(4);
        // 2. 创建 Redisson 实例
        return Redisson.create(config);
    }

}
