package com.sirius.service.trade.settlement.filter;

import com.sirius.repository.TradeRepository;
import com.sirius.model.entity.MarketPayOrderEntity;
import com.sirius.model.entity.TradeSettlementRuleCommandEntity;
import com.sirius.model.entity.TradeSettlementRuleFilterBackEntity;
import com.sirius.service.trade.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.common.design.framework.link.mode2.handler.ILogicHandler;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OutTradeNoRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity,
        TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private TradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        MarketPayOrderEntity order = repository
                .queryMarketPayOrderEntityByOutTradeNo(requestParameter.getUserId(), requestParameter.getOutTradeNo());
        if (order == null) {
            throw new AppException(ResponseCode.E0104);
        }
        dynamicContext.setMarketPayOrderEntity(order);
        return next(requestParameter, dynamicContext);
    }
}
