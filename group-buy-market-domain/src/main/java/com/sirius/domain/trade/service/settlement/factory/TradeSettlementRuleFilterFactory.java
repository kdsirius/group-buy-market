package com.sirius.domain.trade.service.settlement.factory;

import com.sirius.domain.trade.model.entity.GroupBuyTeamEntity;
import com.sirius.domain.trade.model.entity.MarketPayOrderEntity;
import com.sirius.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.sirius.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.sirius.domain.trade.service.settlement.filter.EndRuleFilter;
import com.sirius.domain.trade.service.settlement.filter.OutTradeNoRuleFilter;
import com.sirius.domain.trade.service.settlement.filter.SCRuleFilter;
import com.sirius.domain.trade.service.settlement.filter.SettableRuleFilter;
import com.sirius.types.design.framework.link.mode2.LinkArmory;
import com.sirius.types.design.framework.link.mode2.chain.BusinessLinkedList;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class TradeSettlementRuleFilterFactory {
    @Bean("tradeSettlementRuleFilter")
    public BusinessLinkedList<TradeSettlementRuleCommandEntity, DynamicContext,
            TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter(
            SCRuleFilter scRuleFilter, OutTradeNoRuleFilter outTradeNoRuleFilter,
            SettableRuleFilter settableRuleFilter, EndRuleFilter endRuleFilter) {
        return new LinkArmory<>("交易结算规则过滤链", scRuleFilter,
                outTradeNoRuleFilter, settableRuleFilter, endRuleFilter).getLogicLink();
    }

    @Data
    public static class DynamicContext {
        private MarketPayOrderEntity marketPayOrderEntity;
        private GroupBuyTeamEntity groupBuyTeamEntity;
    }
}
