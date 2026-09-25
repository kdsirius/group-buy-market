package com.sirius.domain.trade.service.lock.filter;

import com.sirius.domain.trade.adapter.repository.ITradeRepository;
import com.sirius.domain.trade.model.entity.GroupBuyActivityEntity;
import com.sirius.domain.trade.model.entity.TradeRuleCommandEntity;
import com.sirius.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.sirius.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.sirius.types.design.framework.link.mode2.handler.ILogicHandler;
import com.sirius.types.enums.ResponseCode;
import com.sirius.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 用户参与限制，规则过滤
 * @date 2026年09月24日 19:43
 */
@Slf4j
@Service
public class UserTakeLimitRuleFilter implements ILogicHandler<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter, TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-用户参与次数校验 userId:{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();

        //查询用户在一个拼团活动上参与的次数
        Integer userTakeOrderCount = repository.queryOrderCountByActivityId(requestParameter.getActivityId(), requestParameter.getUserId());

        if (null != groupBuyActivity.getTakeLimitCount() && userTakeOrderCount >= groupBuyActivity.getTakeLimitCount()) {
            log.info("用户参与次数校验，已达可参与上限 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0103);
        }

        return TradeRuleFilterBackEntity.builder()
                .userTakeOrderCount(userTakeOrderCount)
                .build();
    }
}
