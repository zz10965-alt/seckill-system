package com.zhixian.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.zhixian.seckill.db.dao.OrderDao;
import com.zhixian.seckill.db.dao.SeckillActivityDao;
import com.zhixian.seckill.db.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

/**
 * Handle payment completed messages
 * Deduct inventory
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        //1. Parse the create order request message
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received create order request: " + message);
        Order order = JSON.parseObject(message, Order.class);
        //2. Deduct inventory
        seckillActivityDao.deductStock(order.getSeckillActivityId());
    }
}
