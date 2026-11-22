package com.sirius.service.trade.lock.factory;

import com.sirius.model.entity.GroupBuyActivityEntity;
import com.sirius.model.entity.TradeLockRuleCommandEntity;
import com.sirius.model.entity.TradeLockRuleFilterBackEntity;
import com.sirius.service.trade.lock.filter.ActivityUsabilityRuleFilter;
import com.sirius.service.trade.lock.filter.UserTakeLimitRuleFilter;
import com.sirius.common.design.framework.link.mode2.LinkArmory;
import com.sirius.common.design.framework.link.mode2.chain.BusinessLinkedList;
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
public class TradeLockRuleFilterFactory {
    @Bean("tradeLockRuleFilter")
    public BusinessLinkedList<TradeLockRuleCommandEntity, DynamicContext, TradeLockRuleFilterBackEntity>
    tradeRuleFiler(ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter) {
        return new LinkArmory<>(
                "交易锁单规则过滤链",
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
