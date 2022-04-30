package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3.符合生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4.保存到session
        session.setAttribute("code",code);
        session.setAttribute("phone",phone);
        //5.发送验证码
        log.debug("发送短信验证码成功，验证码：{"+code+"}");
        return Result.ok();
    }
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {

        //1.校验手机号
        if(session.getAttribute("phone")==null || !session.getAttribute("phone").toString().equals(loginForm.getPhone())){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号错误");
        }

        //获取session的验证码
        String code = session.getAttribute("code").toString();
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
        session.setAttribute("user",user);
        return Result.ok();
    }
}
