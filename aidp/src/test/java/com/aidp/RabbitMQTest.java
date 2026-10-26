package com.aidp;

import com.aidp.entity.VoucherOrder;
import com.aidp.utils.RedisIdWorker;
import com.aidp.utils.UserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitMQTest {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Test
    void testRabbitMQ() {
        // 测试 RabbitMQ 相关功能
        String queueName = "order.queue";
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdWorker.nextId("order"));
        voucherOrder.setVoucherId(33L);
        voucherOrder.setUserId(10L);
        rabbitTemplate.convertAndSend(queueName, voucherOrder);
    }
}
