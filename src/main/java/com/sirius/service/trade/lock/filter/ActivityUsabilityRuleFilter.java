package com.sirius.service.trade.lock.filter;

import com.sirius.repository.TradeRepository;
import com.sirius.model.entity.GroupBuyActivityEntity;
import com.sirius.model.entity.TradeLockRuleCommandEntity;
import com.sirius.model.entity.TradeLockRuleFilterBackEntity;
import com.sirius.service.trade.lock.factory.TradeLockRuleFilterFactory;
import com.sirius.common.design.framework.link.mode2.handler.ILogicHandler;
import com.sirius.common.enums.ActivityStatusEnumVO;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Sirius
 * @description 活动的可用性，规则过滤【状态、有效期】
 * @date 2026年09月24日 19:27
 */
@Slf4j
@Service
public class ActivityUsabilityRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private TradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-活动的可用性校验 userId:{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());
        // 查询拼团活动
        GroupBuyActivityEntity groupBuyActivity = repository.queryGroupBuyActivityEntityByActivityId(requestParameter.getActivityId());

        // 校验：活动状态 - 可以跑业务异常code,或者把code写入到动态上下文dynamicContext中，最后获取
        if (!ActivityStatusEnumVO.EFFECTIVE.equals(groupBuyActivity.getStatus())) {
            log.info("活动的可用性校验：非生效状态 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0101);
        }

        // 校验活动时间
        Date currentTime = new Date();
        if (currentTime.before(groupBuyActivity.getStartTime()) || currentTime.after(groupBuyActivity.getEndTime())) {
            log.info("活动的可用性校验：非可参与时间范围 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0102);
        }

        // 写入上下文
        dynamicContext.setGroupBuyActivity(groupBuyActivity);

        // 走到下一个责任链节点
        return next(requestParameter, dynamicContext);
    }
}
