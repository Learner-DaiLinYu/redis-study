package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3.符合生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4.保存到redis
//        session.setAttribute("code",code);
//        session.setAttribute("phone",phone);
        stringRedisTemplate.opsForValue().set("login:code:"+phone,code,2, TimeUnit.MINUTES);

        //5.发送验证码
        log.debug("发送短信验证码成功，验证码：{"+code+"}");
        return Result.ok();
    }
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {

        //1.校验手机号
        if(RegexUtils.isPhoneInvalid(loginForm.getPhone())){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //获取session的验证码
        String code = stringRedisTemplate.opsForValue().get("login:code:"+ loginForm.getPhone());
        if( code==null || !code.equals(loginForm.getCode())){
            return Result.fail("验证码错误");
        }

        //查看是否有手机号
        String phone = loginForm.getPhone();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone,phone);
        User user = this.getOne(wrapper);
        //用户不存在
        if(user==null){
            //创建用户并保存
            user=new User().setPhone(phone).setNickName(USER_NICK_NAME_PREFIX +RandomUtil.randomString(10));
            this.save(user);
        }
//        session.setAttribute("user",new UserDTO().setId(user.getId()).setNickName(user.getNickName()).setIcon(user.getIcon()));
        //保存用户信息到redis中
        //随机生成token，作为登入令牌
        String token = UUID.randomUUID().toString();
        //将User对象转为hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> usermap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true).setFieldValueEditor((fieldName,fieldvalue)->
                            fieldvalue.toString()));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY+token,usermap);
        //返回tocken
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY+token,RedisConstants.LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result logout(String token) {

        System.out.println(RedisConstants.LOGIN_USER_KEY+token);

        stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY+token);
        return Result.ok();
    }
}
