package com.sirius.service.activity.trial.thread;

import com.sirius.repository.ActivityRepository;
import com.sirius.model.value.SkuVO;

import java.util.concurrent.Callable;

/**
 * @author Sirius
 * @description 查询商品信息任务
 * @date 2026年09月19日 14:10
 */
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {

    private final String goodsId;


    private final ActivityRepository activityRepository;

    public QuerySkuVOFromDBThreadTask(String goodsId, ActivityRepository activityRepository) {
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public SkuVO call() throws Exception {
        return activityRepository.querySkuByGoodsId(goodsId);
    }
}
