package com.sirius.service.trade.settlement.filter;

import com.sirius.repository.TradeRepository;
import com.sirius.model.entity.GroupBuyTeamEntity;
import com.sirius.model.entity.TradeSettlementRuleCommandEntity;
import com.sirius.model.entity.TradeSettlementRuleFilterBackEntity;
import com.sirius.service.trade.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.common.design.framework.link.mode2.handler.ILogicHandler;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SettableRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity,
        TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    private TradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyTeamEntity team = repository.queryGroupBuyTeamByTeamId(
                dynamicContext.getMarketPayOrderEntity().getTeamId());

        if (!requestParameter.getOutTradeTime().before(team.getValidEndTime())) {
            throw new AppException(ResponseCode.E0106);
        }
        dynamicContext.setGroupBuyTeamEntity(team);
        return next(requestParameter, dynamicContext);
    }
}
