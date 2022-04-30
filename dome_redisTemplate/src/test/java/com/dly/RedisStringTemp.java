package com.dly;

import com.dly.pojo.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest
public class RedisStringTemp {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void  stringtest1(){
        stringRedisTemplate.opsForValue().set("name","戴林宇");
        String name = stringRedisTemplate.opsForValue().get("name");
        System.out.println("name="+name);
    }

    private static final ObjectMapper mapper=new ObjectMapper();
    @SneakyThrows
    @Test
    public void Objecttest1(){
        //  主动处理
        //手动序列化json放入到redis中
        String userjson = mapper.writeValueAsString(new User("戴林宇", 12));
        stringRedisTemplate.opsForValue().set("user",userjson);
        //读取redis中存入的redis的json字符串 并反序列化成user对象
        String user = stringRedisTemplate.opsForValue().get("user");
        User user1 = mapper.readValue(user, User.class);
        System.out.println("user="+user1);
    }

    @Test
    public void testHash(){
        stringRedisTemplate.opsForHash().put("user:100","name","Jack");
        stringRedisTemplate.opsForHash().put("user:100","age","12");

        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries("user:100");
        System.out.println(entries);
    }
}
