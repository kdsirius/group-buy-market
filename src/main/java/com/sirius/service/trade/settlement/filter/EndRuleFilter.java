package com.sirius.service.trade.settlement.filter;

import com.sirius.model.entity.GroupBuyTeamEntity;
import com.sirius.model.entity.TradeSettlementRuleCommandEntity;
import com.sirius.model.entity.TradeSettlementRuleFilterBackEntity;
import com.sirius.service.trade.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.common.design.framework.link.mode2.handler.ILogicHandler;
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
                .notifyConfigVO(team.getNotifyConfigVO())
                .build();
    }
}
