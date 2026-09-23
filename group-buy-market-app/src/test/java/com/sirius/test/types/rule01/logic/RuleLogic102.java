package com.sirius.test.types.rule01.logic;

import com.sirius.test.types.rule01.factory.Rule01TradeRuleFactory;
import com.sirius.types.design.framework.link.mode1.AbstractLogicLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class RuleLogic102 extends AbstractLogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> {
    @Override
    public String apply(String requestParameter, Rule01TradeRuleFactory.DynamicContext dynamicContext) {
        log.info("link model01 RuleLogic02");
        return "link model01 单实例链";
    }
}
