package com.zhixian.seckill.service;

import com.zhixian.seckill.db.dao.SeckillActivityDao;
import com.zhixian.seckill.db.dao.SeckillCommodityDao;
import com.zhixian.seckill.db.po.SeckillActivity;
import com.zhixian.seckill.db.po.SeckillCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ActivityHtmlPageService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    /**
     * Create the HTML page
     *
     * @throws Exception
     */
    public void createActivityHtml(long seckillActivityId) {

        PrintWriter writer = null;
        try {
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
            SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
            // Get page data
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("seckillActivity", seckillActivity);
            resultMap.put("seckillCommodity", seckillCommodity);
            resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
            resultMap.put("oldPrice", seckillActivity.getOldPrice());
            resultMap.put("commodityId", seckillActivity.getCommodityId());
            resultMap.put("commodityName", seckillCommodity.getCommodityName());
            resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());

            // Create a Thymeleaf context object
            Context context = new Context();
            // Put the data into the context object
            context.setVariables(resultMap);

            // Create the output stream
            File file = new File("src/main/resources/templates/" + "seckill_item_" + seckillActivityId + ".html");
            writer = new PrintWriter(file);
            // Execute the page staticization method
            templateEngine.process("seckill_item", context, writer);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("Page staticization error: " + seckillActivityId);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

