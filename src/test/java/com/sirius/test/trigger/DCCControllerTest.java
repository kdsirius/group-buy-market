package com.sirius.test.trigger;

import com.sirius.controller.DCCController;
import com.sirius.service.market.IMarketService;
import com.alibaba.fastjson.JSON;
import com.sirius.model.dto.request.MarketProductEntity;
import com.sirius.model.vo.TrialBalanceEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 动态配置管理测试
 * @date 2026年09月21日 21:37
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DCCControllerTest {

    @Resource
    private DCCController dccService;

    @Resource
    private IMarketService indexGroupBuyMarketService;

    @Test
    public void test_updateConfig() {
        // 动态调整配置
        dccService.updateConfig("downgradeSwitch", "0");
    }

    @Test
    public void test_updateConfig2indexMarketTrial() throws Exception {
        // 动态调整配置
        dccService.updateConfig("downgradeSwitch", "1");
        // 超时等待异步
        Thread.sleep(1000);

        // 营销验证
        MarketProductEntity marketProductEntity = new MarketProductEntity();
        marketProductEntity.setUserId("xiaofuge");
        marketProductEntity.setSource("s01");
        marketProductEntity.setChannel("c01");
        marketProductEntity.setGoodsId("9890001");

        TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(marketProductEntity);
        log.info("请求参数:{}", JSON.toJSONString(marketProductEntity));
        log.info("返回结果:{}", JSON.toJSONString(trialBalanceEntity));
    }


}

