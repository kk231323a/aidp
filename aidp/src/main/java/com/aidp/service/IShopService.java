package com.aidp.service;

import com.aidp.dto.Result;
import com.aidp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);
}
