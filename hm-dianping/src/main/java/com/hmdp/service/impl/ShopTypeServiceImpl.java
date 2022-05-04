package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getlist() {
        String json = stringRedisTemplate.opsForValue().get("cache:shoplist");
        if(StrUtil.isNotBlank(json)){
            List<ShopType> shopTypes = JSONUtil.toList(json, ShopType.class);
            return Result.ok(shopTypes);
        }
        List<ShopType> list = this.list();
        if(list.isEmpty()) return Result.fail("没有数据");
        //数据添加到缓存
        stringRedisTemplate.opsForValue().set("cache:shoplist",JSONUtil.toJsonStr(list));
        return Result.ok(list);
    }
}
