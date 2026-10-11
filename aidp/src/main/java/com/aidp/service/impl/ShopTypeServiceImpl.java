package com.aidp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aidp.dto.Result;
import com.aidp.entity.ShopType;
import com.aidp.mapper.ShopTypeMapper;
import com.aidp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getShopTypes() {
        //1.查询redis是否有缓存
        String shopTypes = stringRedisTemplate.opsForValue().get("cache:shopType");
        //2.判断如果有，直接返回
        if (StrUtil.isNotBlank(shopTypes)) {
            List<ShopType> shopTypeList = JSONUtil.toList(shopTypes, ShopType.class);
            return Result.ok(shopTypeList);
        }
        //3.如果没有，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();

        //4.如果没有，返回错误
        if (shopTypeList == null) {
            return Result.fail("店铺类型不存在");
        }

        //5.如果有，写入redis缓存
        stringRedisTemplate.opsForValue().set("cache:shopType", JSONUtil.toJsonStr(shopTypeList));

        //6.返回+

        return Result.ok(shopTypeList);
    }
}
