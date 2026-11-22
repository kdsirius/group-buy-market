package com.sirius.service.trade.lock.filter;

import com.sirius.repository.TradeRepository;
import com.sirius.model.entity.GroupBuyActivityEntity;
import com.sirius.model.entity.TradeLockRuleCommandEntity;
import com.sirius.model.entity.TradeLockRuleFilterBackEntity;
import com.sirius.service.trade.lock.factory.TradeLockRuleFilterFactory;
import com.sirius.common.design.framework.link.mode2.handler.ILogicHandler;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
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
public class UserTakeLimitRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private TradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-用户参与次数校验 userId:{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();

        //查询用户在一个拼团活动上参与的次数
        Integer userTakeOrderCount = repository.queryOrderCountByActivityId(requestParameter.getActivityId(), requestParameter.getUserId());

        if (null != groupBuyActivity.getTakeLimitCount() && userTakeOrderCount >= groupBuyActivity.getTakeLimitCount()) {
            log.info("用户参与次数校验，已达可参与上限 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0103);
        }

        return TradeLockRuleFilterBackEntity.builder()
                .userTakeOrderCount(userTakeOrderCount)
                .build();
    }
}
