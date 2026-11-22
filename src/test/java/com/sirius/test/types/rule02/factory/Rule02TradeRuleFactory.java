package com.sirius.test.types.rule02.factory;

import com.sirius.test.types.rule02.logic.RuleLogic201;
import com.sirius.test.types.rule02.logic.RuleLogic202;
import com.sirius.test.types.rule02.logic.XxxResponse;
import com.sirius.common.design.framework.link.mode2.LinkArmory;
import com.sirius.common.design.framework.link.mode2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


@Service
public class Rule02TradeRuleFactory {
    @Bean("demo01")
    public BusinessLinkedList<String, DynamicContext, XxxResponse> demo01(RuleLogic201 ruleLogic201, RuleLogic202 ruleLogic202) {
        return new LinkArmory<>("demo01", ruleLogic201, ruleLogic202).getLogicLink();
    }

    @Bean("demo02")
    public BusinessLinkedList<String, DynamicContext, XxxResponse> demo02(RuleLogic202 ruleLogic202) {
        return new LinkArmory<>("demo02", ruleLogic202).getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private String age;
    }

}
