package com.sirius.domain.trade.service.settlement.filter;


import com.sirius.domain.trade.adapter.repository.ITradeRepository;
import com.sirius.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.sirius.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.sirius.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.types.design.framework.link.mode2.handler.ILogicHandler;
import com.sirius.types.enums.ResponseCode;
import com.sirius.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SCRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity,
        TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        if (repository.isSCBlackIntercept(requestParameter.getSource(), requestParameter.getChannel())) {
            throw new AppException(ResponseCode.E0105);
        }
        return next(requestParameter, dynamicContext);
    }
}
