package com.aidp.service;

import com.aidp.dto.Result;
import com.aidp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId) ;

    void createVoucherOrder(VoucherOrder voucherOrder);
}
