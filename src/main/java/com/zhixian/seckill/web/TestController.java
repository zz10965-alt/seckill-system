package com.zhixian.seckill.web;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class TestController {

    @ResponseBody
    @RequestMapping("hello")
    public String hello(){
        String result;
        // The resource name can be any string with business semantics, such as a method name, an interface name, or any other string that uniquely identifies it.
        try (Entry entry = SphU.entry("HelloResource")){
            // The protected business logic
            result  = "Hello Sentinel";
            return result;
        }catch (BlockException ex) {
            // Resource access is blocked, rate-limited, or degraded
            // Perform the corresponding handling here
            log.error(ex.toString());
            result = "System is busy, please try again later";
            return  result;
        }
    }

    /**
     *  Define rate limiting rules
     *  1. Create a collection to store the rate limiting rules
     *  2. Create the rate limiting rules
     *  3. Add the rate limiting rules to the collection
     *  4. Load the rate limiting rules
     *  @PostConstruct executes after the constructor of the current class completes
     */
    @PostConstruct
    public void seckillsFlow(){
        //1. Create a collection to store the rate limiting rules
        List<FlowRule> rules = new ArrayList<>();
        //2. Create the rate limiting rule
        FlowRule rule = new FlowRule();
        // Define the resource, indicating which resource Sentinel will protect
        rule.setResource("seckills");
        // Define the rate limiting rule type: QPS
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Define the number of requests allowed per second (QPS)
        rule.setCount(1);

        FlowRule rule2 = new FlowRule();
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(2);
        rule2.setResource("HelloResource");
        //3. Add the rate limiting rules to the collection
        rules.add(rule);
        rules.add(rule2);
        //4. Load the rate limiting rules
        FlowRuleManager.loadRules(rules);
    }
}