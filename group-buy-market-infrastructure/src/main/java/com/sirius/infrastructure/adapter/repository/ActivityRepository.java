package com.sirius.infrastructure.adapter.repository;

import com.sirius.domain.activity.adapter.repository.IActivityRepository;
import com.sirius.domain.activity.model.valobj.DiscountTypeEnum;
import com.sirius.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.sirius.domain.activity.model.valobj.SCSkuActivityVO;
import com.sirius.domain.activity.model.valobj.SkuVO;
import com.sirius.infrastructure.dao.IGroupBuyActivityDao;
import com.sirius.infrastructure.dao.IGroupBuyDiscountDao;
import com.sirius.infrastructure.dao.ISCSkuActivityDao;
import com.sirius.infrastructure.dao.ISkuDao;
import com.sirius.infrastructure.dao.po.GroupBuyActivity;
import com.sirius.infrastructure.dao.po.GroupBuyDiscount;
import com.sirius.infrastructure.dao.po.SCSkuActivity;
import com.sirius.infrastructure.dao.po.Sku;
import com.sirius.infrastructure.dcc.DCCService;
import com.sirius.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 活动仓储
 * @date 2026年09月19日 13:36
 */
@Repository
public class ActivityRepository implements IActivityRepository {
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Resource
    private ISkuDao skuDao;

    @Resource
    private ISCSkuActivityDao scSkuActivityDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private DCCService dccService;

    @Override
    public boolean isTagCrowdRange(String tagId, String userId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) return true;
        // 判断用户是否在人群中
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId) {
        SCSkuActivity scSkuActivityReq = new SCSkuActivity();
        scSkuActivityReq.setSource(source);
        scSkuActivityReq.setChannel(channel);
        scSkuActivityReq.setGoodsId(goodsId);

        SCSkuActivity scSkuActivityRes = scSkuActivityDao.querySCSkuActivityBySCGoodsId(scSkuActivityReq);
        if (scSkuActivityRes == null) return null;

        return SCSkuActivityVO.builder()
                .source(scSkuActivityRes.getSource())
                .chanel(scSkuActivityRes.getChannel())
                .activityId(scSkuActivityRes.getActivityId())
                .goodsId(scSkuActivityRes.getGoodsId())
                .build();
    }

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {
        GroupBuyActivity groupBuyActivityRes = groupBuyActivityDao.queryValidGroupBuyActivityByActivityId(activityId);
        if (groupBuyActivityRes == null) return null;

        String discountId = groupBuyActivityRes.getDiscountId();
        GroupBuyDiscount groupBuyDiscountRes = groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(discountId);
        if (groupBuyDiscountRes == null) return null;

        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountName(groupBuyDiscountRes.getDiscountName())
                .discountDesc(groupBuyDiscountRes.getDiscountDesc())
                .discountType(DiscountTypeEnum.get(groupBuyDiscountRes.getDiscountType()))
                .marketPlan(groupBuyDiscountRes.getMarketPlan())
                .marketExpr(groupBuyDiscountRes.getMarketExpr())
                .tagId(groupBuyDiscountRes.getTagId())
                .build();

        return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivityRes.getActivityId())
                .activityName(groupBuyActivityRes.getActivityName())
                .groupBuyDiscount(groupBuyDiscountVO)
                .groupType(groupBuyActivityRes.getGroupType())
                .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
                .target(groupBuyActivityRes.getTarget())
                .validTime(groupBuyActivityRes.getValidTime())
                .status(groupBuyActivityRes.getStatus())
                .startTime(groupBuyActivityRes.getStartTime())
                .endTime(groupBuyActivityRes.getEndTime())
                .tagId(groupBuyActivityRes.getTagId())
                .tagScope(groupBuyActivityRes.getTagScope())
                .build();
    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (sku == null) return null;
        return SkuVO.builder()
                .goodsId(goodsId)
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();
    }

    @Override
    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }
}
