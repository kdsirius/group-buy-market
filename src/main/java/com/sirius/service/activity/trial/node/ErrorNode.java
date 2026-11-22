package com.sirius.service.activity.trial.node;

import com.alibaba.fastjson.JSON;
import com.sirius.model.entity.MarketProductEntity;
import com.sirius.model.entity.TrialBalanceEntity;
import com.sirius.service.activity.trial.AbstractGroupBuyMarketSupport;
import com.sirius.service.activity.trial.factory.DefaultActivityStrategyFactory;
import com.sirius.common.design.framework.tree.StrategyHandler;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Sirius
 * @description 异常节点处理；无营销、流程降级、超时调用等，都可以路由到 ErrorNode 节点统一处理
 * @date 2026年09月20日 12:52
 */
@Slf4j
@Service
public class ErrorNode extends AbstractGroupBuyMarketSupport {
    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) {
        log.info("拼团商品查询试算服务-NoMarketNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));

        // 无营销配置
        if (null == dynamicContext.getGroupBuyActivityDiscountVO() || null == dynamicContext.getSkuVO()) {
            log.info("商品无拼团营销配置 {}", requestParameter.getGoodsId());
            throw new AppException(ResponseCode.E0002.getCode(), ResponseCode.E0002.getInfo());
        }

        return TrialBalanceEntity.builder().build();
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
