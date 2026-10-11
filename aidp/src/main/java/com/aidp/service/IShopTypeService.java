package com.aidp.service;

import com.aidp.dto.Result;
import com.aidp.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopTypeService extends IService<ShopType> {

    Result getShopTypes();
}
