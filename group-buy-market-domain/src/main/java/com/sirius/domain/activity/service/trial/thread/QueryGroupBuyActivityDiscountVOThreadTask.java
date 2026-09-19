package com.sirius.domain.activity.service.trial.thread;

import com.sirius.domain.activity.adapter.repository.IActivityRepository;
import com.sirius.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.util.concurrent.Callable;

/**
 * @author Sirius
 * @description 查询营销配置任务
 * @date 2026年09月19日 14:07
 */
public class QueryGroupBuyActivityDiscountVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {

    // 来源
    private final String source;

    // 渠道
    private final String channel;

    // 活动仓储
    private final IActivityRepository activityRepository;

    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.activityRepository = activityRepository;
    }

    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        return activityRepository.queryGroupBuyActivityDiscountVO(source, channel);
    }
}
