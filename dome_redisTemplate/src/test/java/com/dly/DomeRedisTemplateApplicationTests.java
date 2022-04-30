package com.dly;

import com.dly.pojo.User;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;

@SpringBootTest
class DomeRedisTemplateApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        /**
         * opsForValue()  --->  string
         * opsForHash()  ---->   hash
         * opsForList()  ----->   list
         * opsForSet() ------>   set
         * opsForZSet     ----->  Zset
         * keys *
         */
        redisTemplate.opsForValue().set("user",new User("戴林宇",12));
        Object user = redisTemplate.opsForValue().get("user");
        System.out.println("user=" + user);
    }
}
