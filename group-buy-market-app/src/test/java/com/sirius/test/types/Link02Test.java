package com.sirius.test.types;

import com.sirius.test.types.rule02.factory.Rule02TradeRuleFactory;
import com.sirius.test.types.rule02.logic.XxxResponse;
import com.sirius.types.design.framework.link.mode2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class Link02Test {

    @Resource
    private Map<String, BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse>> businessLinkedListMap;

    @Test

    public void test_mode02_demo01() throws Exception {
        log.info("test_mode02_demo01");
        BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> demo01 = businessLinkedListMap.get("demo01");
        XxxResponse response = demo01.apply("test", Rule02TradeRuleFactory.DynamicContext.builder().age("18").build());
        log.info("test_mode02_demo01 response:{}", response.getAge());
    }

    @Test
    public void test_mode02_demo02() throws Exception {
        log.info("test_mode02_demo02");
        BusinessLinkedList<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> demo01 = businessLinkedListMap.get("demo02");
        XxxResponse response = demo01.apply("test", Rule02TradeRuleFactory.DynamicContext.builder().age("20").build());
        log.info("test_mode02_demo02 response:{}", response.getAge());
    }
}
