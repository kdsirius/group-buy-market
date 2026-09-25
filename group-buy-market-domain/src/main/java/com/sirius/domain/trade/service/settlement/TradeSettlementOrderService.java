package com.sirius.domain.trade.service.settlement;

import com.sirius.domain.trade.adapter.repository.ITradeRepository;
import com.sirius.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.sirius.domain.trade.model.entity.*;
import com.sirius.domain.trade.service.ITradeSettlementOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 拼团交易结算服务
 * @date 2026年09月25日 11:20
 */
@Slf4j
@Service
public class TradeSettlementOrderService implements ITradeSettlementOrderService {
    @Resource
    private ITradeRepository repository;

    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) {
        log.info("拼团交易-支付订单结算 userId:{} outTradeNo:{}", tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
        //1.查询拼团信息
        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketPayOrderEntityByOutTradeNo(tradePaySuccessEntity.getUserId(),
                tradePaySuccessEntity.getOutTradeNo());
        if (null == marketPayOrderEntity) {
            log.info("不存在的外部交易但后或用户已退单，不需要做支付订单结算 userId:{} outTradeNo:{}", tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
            return null;
        }

        //2. 查询组团信息
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());

        //3. 构建聚合对象
        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = GroupBuyTeamSettlementAggregate.builder()
                .groupBuyTeamEntity(groupBuyTeamEntity)
                .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .build();

        //4. 拼团交易结算
        repository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);

        //5. 返回结算信息 - 公司中开发这样的流程时候，会根据外部需要进行值的设置
        return TradePaySettlementEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .teamId(marketPayOrderEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build();
    }
}
