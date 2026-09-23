package com.sirius.test.types.rule01.factory;

import com.sirius.test.types.rule01.logic.RuleLogic101;
import com.sirius.test.types.rule01.logic.RuleLogic102;
import com.sirius.types.design.framework.link.mode1.ILogicLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class Rule01TradeRuleFactory {

    @Resource
    private RuleLogic101 ruleLogic01;

    @Resource
    private RuleLogic102 ruleLogic02;

    public ILogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> openLogicLink() {
        ruleLogic01.appendNext(ruleLogic02);
        return ruleLogic01;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private String age;
    }
}
