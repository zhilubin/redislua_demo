package com.qf.redis_lua;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.qf")
@MapperScan(basePackages = "com.qf.dao")
public class RedisLuaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisLuaApplication.class, args);
    }

}
