package com.example.park;

import com.example.park.util.RedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Random;

@MapperScan("com.example.park.dao")
@SpringBootApplication
public class ParkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder onCreateBCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RedisUtil onCreateRedisUtil(){
        return new RedisUtil();
    }

    /**
     * 生成随机数
     * @return
     */
    @Bean
    public Random onCreateRandom(){ return new Random();}

}
