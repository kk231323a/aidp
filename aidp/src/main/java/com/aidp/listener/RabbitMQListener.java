package com.aidp.listener;

import com.aidp.entity.VoucherOrder;
import com.aidp.service.impl.VoucherOrderServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Slf4j
@Component
public class RabbitMQListener {

    @Autowired
    private VoucherOrderServiceImpl voucherOrderService;
    @RabbitListener(queues = "order.queue")
    public void listenOrderQueue(VoucherOrder voucherOrder ,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // 处理订单
        try {
            voucherOrderService.handleVoucherOrder(voucherOrder);
        }catch (Exception e){
               log.error("订单消息处理失败，deliveryTag={}", deliveryTag, e);
               channel.basicNack(deliveryTag, false, true);
               return ;
        }
            // 手动确认消息
            channel.basicAck(deliveryTag, false);



    }
}
