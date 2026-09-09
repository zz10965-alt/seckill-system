package com.zhixian.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.zhixian.seckill.db.dao.OrderDao;
import com.zhixian.seckill.db.dao.SeckillActivityDao;
import com.zhixian.seckill.db.po.Order;
import com.zhixian.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "seckill_order", consumerGroup = "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    RedisService redisService;

    @Override
    @Transactional
    public void onMessage (MessageExt messageExt) {
        //1. Parse the create order request message
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received create order request: " + message);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(new Date());
        //2. Deduct inventory
        boolean lockStockResult = seckillActivityDao.lockStock(order.getSeckillActivityId());
        if (lockStockResult) {
            // Order status: 0 = no available inventory, invalid order; 1 = created, awaiting payment
            order.setOrderStatus(1);
            // Add the user to the purchase-limited user list
            redisService.addLimitMember(order.getSeckillActivityId(), order.getUserId());
        } else {
            order.setOrderStatus(0);
        }
        //3. Insert the order
        orderDao.insertOrder(order);
    }
}
