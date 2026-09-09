package com.zhixian.seckill.web;

import com.zhixian.seckill.service.SeckillActivityService;
import com.zhixian.seckill.services.SeckillOverSellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SeckillOverSellController {

    @Autowired
    private SeckillOverSellService seckillOverSellService;



    /**
     * Simple handling of seckill requests
     * @param seckillActivityId
     * @return
     */
//    @ResponseBody
//    @RequestMapping("/seckill/{seckillActivityId}")
    public String  seckil(@PathVariable long seckillActivityId){
        return seckillOverSellService.processSeckill(seckillActivityId);
    }
    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * Use a Lua script to handle seckill requests
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckillCommodity(@PathVariable long seckillActivityId) {
        boolean stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
        return stockValidateResult ? "Congratulations! Your seckill order was placed successfully" : "The item is sold out, please try again next time";
    }
}
