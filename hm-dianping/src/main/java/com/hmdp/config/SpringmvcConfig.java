package com.hmdp.config;

import com.hmdp.Interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class SpringmvcConfig implements WebMvcConfigurer {

    @Resource  //通过对象名来装配  @Autowired 通过类型来匹配 如果一个类由多个实例 可以通过@Qualifier来指定名字
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/shop-type/**",
                        "/shop/**",
                        "/voucher/**",
                        "/upload/**"
                );
    }
}
