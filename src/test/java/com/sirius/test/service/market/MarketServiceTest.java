package com.sirius.test.service.market;

import com.sirius.service.market.IMarketService;
import com.alibaba.fastjson.JSON;
import com.sirius.model.dto.request.MarketProductRequest;
import com.sirius.model.vo.TrialBalanceVO;
import com.sirius.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 首页营销服务接口测试
 * @date 2026年09月19日 10:30
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MarketServiceTest {

    @Resource
    private IMarketService indexGroupBuyMarketService;

    @Test
    public void test_indexMarketTrial() throws Exception {
        MarketProductRequest marketProductEntity = new MarketProductRequest();
        marketProductEntity.setUserId("sirius");
        marketProductEntity.setSource("s01");
        marketProductEntity.setChannel("c01");
        marketProductEntity.setGoodsId("9890001");

        TrialBalanceVO trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(marketProductEntity);
        log.info("请求参数:{}", JSON.toJSONString(marketProductEntity));
        log.info("返回结果:{}", JSON.toJSONString(trialBalanceEntity));
    }

    @Test
    public void test_indexMarketTrial_no_tag() throws Exception {
        MarketProductRequest marketProductEntity = new MarketProductRequest();
        marketProductEntity.setUserId("dacihua");
        marketProductEntity.setSource("s01");
        marketProductEntity.setChannel("c01");
        marketProductEntity.setGoodsId("9890001");

        TrialBalanceVO trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(marketProductEntity);
        log.info("请求参数:{}", JSON.toJSONString(marketProductEntity));
        log.info("返回结果:{}", JSON.toJSONString(trialBalanceEntity));
    }


    @Test
    public void test_indexMarketTrial_error() throws Exception {
        MarketProductRequest marketProductEntity = new MarketProductRequest();
        marketProductEntity.setUserId("xiaofuge");
        marketProductEntity.setSource("s01");
        marketProductEntity.setChannel("c01");
        marketProductEntity.setGoodsId("9890002");

        Assert.assertThrows(AppException.class, () -> indexGroupBuyMarketService.indexMarketTrial(marketProductEntity));
    }

}
