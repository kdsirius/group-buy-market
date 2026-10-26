package com.sirius.domain.trade.service.settlement.filter;

import com.sirius.domain.trade.model.entity.GroupBuyTeamEntity;
import com.sirius.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.sirius.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.sirius.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.types.design.framework.link.mode2.handler.ILogicHandler;
import org.springframework.stereotype.Service;

@Service
public class EndRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity,
        TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyTeamEntity team = dynamicContext.getGroupBuyTeamEntity();
        return TradeSettlementRuleFilterBackEntity.builder()
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .targetCount(team.getTargetCount())
                .completeCount(team.getCompleteCount())
                .lockCount(team.getLockCount())
                .status(team.getStatus())
                .validStartTime(team.getValidStartTime())
                .validEndTime(team.getValidEndTime())
                .build();
    }
}
