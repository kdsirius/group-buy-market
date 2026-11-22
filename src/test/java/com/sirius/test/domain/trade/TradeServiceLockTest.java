package com.sirius.test.domain.trade;

import com.sirius.service.trade.ITradeService;
import com.alibaba.fastjson.JSON;
import com.sirius.model.dto.request.LockMarketPayOrderRequestDTO;
import com.sirius.model.dto.response.LockMarketPayOrderResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 交易订单锁单服务测试
 * @date 2026年09月22日19:56
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TradeServiceLockTest {

    @Resource
    private ITradeService tradeService;

    @Test
    public void test_lockMarketPayOrder() throws Exception {
        // 入参信息
        Long activityId = 100123L;
        String userId = "xfg05";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String outTradeNo = "909000098121";

        // 组装锁单请求
        LockMarketPayOrderRequestDTO request = new LockMarketPayOrderRequestDTO();
        request.setUserId(userId);
        request.setActivityId(activityId);
        request.setGoodsId(goodsId);
        request.setSource(source);
        request.setChannel(channel);
        request.setOutTradeNo(outTradeNo);

        LockMarketPayOrderRequestDTO.NotifyConfigVO notifyConfig =
                new LockMarketPayOrderRequestDTO.NotifyConfigVO();
        notifyConfig.setNotifyType("HTTP");
        notifyConfig.setNotifyUrl("http://127.0.0.1:8091/api/v1/test/group_buy_notify");
        request.setNotifyConfigVO(notifyConfig);

        LockMarketPayOrderResponseDTO result = tradeService.lockMarketPayOrder(request);
        log.info("??????:{}", JSON.toJSONString(result));
    }

}
