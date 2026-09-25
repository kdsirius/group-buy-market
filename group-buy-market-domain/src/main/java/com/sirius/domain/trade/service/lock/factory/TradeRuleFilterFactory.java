package com.sirius.domain.trade.service.lock.factory;

import com.sirius.domain.trade.model.entity.GroupBuyActivityEntity;
import com.sirius.domain.trade.model.entity.TradeRuleCommandEntity;
import com.sirius.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.sirius.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.sirius.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import com.sirius.types.design.framework.link.mode2.LinkArmory;
import com.sirius.types.design.framework.link.mode2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author Sirius
 * @description 交易规则过滤工厂
 * @date 2026年09月24日 19:24
 */
@Slf4j
@Service
public class TradeRuleFilterFactory {
    @Bean("tradeRuleFilter")
    public BusinessLinkedList<TradeRuleCommandEntity, DynamicContext, TradeRuleFilterBackEntity>
    tradeRuleFiler(ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter) {
        return new LinkArmory<>(
                "交易规则过滤链",
                activityUsabilityRuleFilter,
                userTakeLimitRuleFilter
        ).getLogicLink();

    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext {

        private GroupBuyActivityEntity groupBuyActivity;

    }
}
