package com.aidp.listener;

import com.aidp.entity.VoucherOrder;
import com.aidp.service.impl.VoucherOrderServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    @Autowired
    private VoucherOrderServiceImpl voucherOrderService;
    @RabbitListener(queues = "order.queue")
    public void listenOrderQueue(VoucherOrder voucherOrder) {
        // 处理订单
        voucherOrderService.createVoucherOrder(voucherOrder);

    }
}
