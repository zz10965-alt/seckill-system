package com.zhixian.seckill.db.dao;

import com.zhixian.seckill.db.po.Order;

public interface OrderDao {

    void insertOrder(Order order);

    Order queryOrder(String orderNo);

    void updateOrder(Order order);
}
