package com.zhixian.seckill;

import com.zhixian.seckill.service.SeckillActivityService;
import com.zhixian.seckill.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.UUID;

@SpringBootTest
public class RedisDemoTest {

    @Resource
    private RedisService redisService;
    @Resource
    SeckillActivityService seckillActivityService;

    @Test
    public void stockTest() {
        redisService.setValue("stock:19", 10L);
    }

    @Test
    public void getStockTest() {
        String stock = redisService.getValue("stock:19");
        System.out.println(stock);
    }

    @Test
    public void stockDeductValidatorTest() {
        boolean result = redisService.stockDeductValidator("stock:19");
        System.out.println("result:" + result);
        String stock = redisService.getValue("stock:19");
        System.out.println("stock:" + stock);
    }


    @Test
    public void revertStock() {
        String stock = redisService.getValue("stock:19");
        System.out.println("Inventory before reverting: " + stock);

        redisService.revertStock("stock:19");

        stock = redisService.getValue("stock:19");
        System.out.println("Inventory after reverting: " + stock);
    }

    @Test
    public void removeLimitMember() {
        redisService.removeLimitMember(19, 1234);
    }

    @Test
    public void pushSeckillInfoToRedisTest(){
        seckillActivityService.pushSeckillInfoToRedis(19);
    }
    @Test
    public void getSekillInfoFromRedis() {
        String seclillInfo = redisService.getValue("seckillActivity:" + 19);
        System.out.println(seclillInfo);
        String seckillCommodity = redisService.getValue("seckillCommodity:"+1001);
        System.out.println(seckillCommodity);
    }

    /**
     * Test the result of acquiring the lock under high concurrency
     */
    @Test
    public void  testConcurrentAddLock() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // Printed result: true false false false false false false false false false
            // Only the first one can acquire the lock
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
        }
    }
    /**
     * Test the result of acquiring and immediately releasing the lock under concurrency
     */
    @Test
    public void  testConcurrent() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // Printed result: true true true true true true true true true true
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
            redisService.releaseDistributedLock("A", requestId);
        }
    }

}
