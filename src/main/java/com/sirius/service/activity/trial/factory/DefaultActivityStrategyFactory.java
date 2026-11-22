package com.sirius.service.activity.trial.factory;

import com.sirius.model.entity.MarketProductEntity;
import com.sirius.model.entity.TrialBalanceEntity;
import com.sirius.model.value.GroupBuyActivityDiscountVO;
import com.sirius.model.value.SkuVO;
import com.sirius.service.activity.trial.node.RootNode;
import com.sirius.common.design.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Sirius
 * @description 活动策略工厂
 * @date 2026年09月18日 17:05
 */
@Service
public class DefaultActivityStrategyFactory {

    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        // 拼团活动营销配置对象
        private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;
        // 商品信息
        private SkuVO skuVO;
        // 折扣价格
        private BigDecimal deductionPrice;
        // 支付价格
        private BigDecimal payPrice;
        // 活动可见性限制
        private boolean visible;
        // 活动
        private boolean enable;

    }
}
