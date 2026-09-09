package com.zhixian.seckill.service;

import com.alibaba.fastjson.JSON;
import com.zhixian.seckill.db.dao.OrderDao;
import com.zhixian.seckill.db.dao.SeckillActivityDao;
import com.zhixian.seckill.db.dao.SeckillCommodityDao;
import com.zhixian.seckill.db.po.Order;
import com.zhixian.seckill.db.po.SeckillActivity;
import com.zhixian.seckill.db.po.SeckillCommodity;
import com.zhixian.seckill.mq.RocketMQService;
import com.zhixian.seckill.util.RedisService;
import com.zhixian.seckill.util.SnowFlake;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    SeckillCommodityDao seckillCommodityDao;

    @Autowired
    OrderDao orderDao;

    /**
     * datacenterId;  Data center
     * machineId;     Machine ID
     * In a distributed environment these can be read from machine configuration
     * Hardcoded here for the standalone development environment
     */
    private final SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * Create order.
     *
     * @param id Activity ID
     * @param userId User ID
     * @return Order detail
     * @throws Exception MQ exception
     */
    public Order createOrder(long id, long userId) throws Exception {

        // 1. query & get activity
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(id);

        // 2. create & set new order information
        Order order = new Order();

        // use snowflake algorithm to generate order ID
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());

        // 3. send "create order" message to Rocket MQ
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        // 4. send "validate pay status" message to Rocket MQ
        // Rocket MQ support 18 levels, messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order), 5);

        return order;
    }

    /**
     * Check whether the item still has inventory
     *
     * @param activityId Item ID
     * @return
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }


    /**
     * Push seckill detail information into Redis
     *
     * @param seckillActivityId
     */
    public void pushSeckillInfoToRedis(long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:" + seckillActivityId, JSON.toJSONString(seckillActivity));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(), JSON.toJSONString(seckillCommodity));
    }

    /**
     * Handle completed order payment
     *
     * @param orderNo
     */
    public void payOrderProcess(String orderNo) throws Exception {
        log.info("Order payment completed, order number: " + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        /*
         * 1. Check whether the order exists
         * 2. Check whether the order status is unpaid
         */
        if (order == null) {
            log.error("Order not found for order number: " + orderNo);
            return;
        } else if(order.getOrderStatus() != 1 ) {
            log.error("Invalid order status: " + orderNo);
            return;
        }
        /*
         * 2. Mark the order as paid
         */
        order.setPayTime(new Date());
        // Order status: 0 = no available inventory, invalid order; 1 = created, awaiting payment; 2 = payment completed
        order.setOrderStatus(2);
        orderDao.updateOrder(order);
        /*
         * 3. Send the order payment success message
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }
}
