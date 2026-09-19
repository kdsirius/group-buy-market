package com.sirius.domain.activity.service.trial.thread;

import com.sirius.domain.activity.adapter.repository.IActivityRepository;
import com.sirius.domain.activity.model.valobj.SkuVO;

import java.util.concurrent.Callable;

/**
 * @author Sirius
 * @description 查询商品信息任务
 * @date 2026年09月19日 14:10
 */
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {

    private final String goodsId;


    private final IActivityRepository activityRepository;

    public QuerySkuVOFromDBThreadTask(String goodsId, IActivityRepository activityRepository) {
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public SkuVO call() throws Exception {
        return activityRepository.querySkuByGoodsId(goodsId);
    }
}
