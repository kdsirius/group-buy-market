package com.sirius.test.domain.trade;

import com.sirius.service.trade.ITradeService;
import com.alibaba.fastjson.JSON;
import com.sirius.model.dto.response.SettlementMarketPayOrderResponseDTO;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Sirius
 * @description 交易订单结算服务测试
 * @date 2026年09月25日 15:53
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TradeServiceSettlementTest {

    @Resource
    private ITradeService tradeService;

    @Test
    public void test_settlementMarketPayOrder() throws Exception {
        SettlementMarketPayOrderRequestDTO tradePaySuccessEntity = new SettlementMarketPayOrderRequestDTO();
        tradePaySuccessEntity.setSource("s01");
        tradePaySuccessEntity.setChannel("c01");
        tradePaySuccessEntity.setUserId("xfg01");
        tradePaySuccessEntity.setOutTradeNo("909000098119");
        tradePaySuccessEntity.setOutTradeTime(new Date());

        SettlementMarketPayOrderResponseDTO tradePaySettlementEntity = tradeService.settlementMarketPayOrder(tradePaySuccessEntity);

        log.info("请求参数：{}", JSON.toJSONString(tradePaySuccessEntity));
        log.info("响应结果：{}", JSON.toJSONString(tradePaySettlementEntity));
    }
}

