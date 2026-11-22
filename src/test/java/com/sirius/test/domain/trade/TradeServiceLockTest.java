package com.sirius.test.domain.trade;

import com.sirius.service.market.IMarketService;
import com.sirius.service.trade.ITradeService;
import com.alibaba.fastjson.JSON;
import com.sirius.model.dto.request.MarketProductEntity;
import com.sirius.model.vo.TrialBalanceEntity;
import com.sirius.model.vo.GroupBuyActivityDiscountVO;
import com.sirius.model.vo.MarketPayOrderEntity;
import com.sirius.model.dto.request.PayActivityEntity;
import com.sirius.model.dto.request.PayDiscountEntity;
import com.sirius.model.vo.NotifyConfigVO;
import com.sirius.model.enums.NotifyTypeEnumVO;
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
    private IMarketService indexGroupBuyMarketService;

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

        // 1. 获取试算优惠，有【activityId】优先使用
        TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                .userId(userId)
                .source(source)
                .channel(channel)
                .goodsId(goodsId)
                .activityId(activityId)
                .build());

        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

        // 查询 outTradeNo 是否已经存在交易记录
        MarketPayOrderEntity marketPayOrderEntityOld = tradeService.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
        if (null != marketPayOrderEntityOld) {
            log.info("测试结果(Old):{}", JSON.toJSONString(marketPayOrderEntityOld));
            return;
        }

        // 2. 锁定，营销预支付订单；商品下单前，预购锁定。
        MarketPayOrderEntity marketPayOrderEntityNew = tradeService.lockMarketPayOrder(
                userId,
                PayActivityEntity.builder()
                        .teamId(null)
                        .activityId(groupBuyActivityDiscountVO.getActivityId())
                        .activityName(groupBuyActivityDiscountVO.getActivityName())
                        .startTime(groupBuyActivityDiscountVO.getStartTime())
                        .endTime(groupBuyActivityDiscountVO.getEndTime())
                        .targetCount(groupBuyActivityDiscountVO.getTarget())
                        .validTime(groupBuyActivityDiscountVO.getValidTime())
                        .build(),
                PayDiscountEntity.builder()
                        .source(source)
                        .channel(channel)
                        .goodsId(goodsId)
                        .goodsName(trialBalanceEntity.getGoodsName())
                        .originalPrice(trialBalanceEntity.getOriginalPrice())
                        .deductionPrice(trialBalanceEntity.getDeductionPrice())
                        .payPrice(trialBalanceEntity.getPayPrice())
                        .outTradeNo(outTradeNo)
                        .notifyConfigVO(NotifyConfigVO.builder()
                                .notifyType(NotifyTypeEnumVO.HTTP)
                                .notifyUrl("http://127.0.0.1:8091/api/v1/test/group_buy_notify")
                                .build())
                        .build());

        log.info("测试结果(New):{}",JSON.toJSONString(marketPayOrderEntityNew));
    }

}
