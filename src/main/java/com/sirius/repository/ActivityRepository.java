package com.sirius.repository;

import com.sirius.model.entity.UserGroupBuyOrderDetailEntity;
import com.sirius.model.value.*;
import com.sirius.mapper.*;
import com.sirius.model.entity.*;
import com.sirius.config.dcc.DCCService;
import com.sirius.client.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sirius
 * @description 活动仓储
 * @date 2026年09月19日 13:36
 */
@Repository
public class ActivityRepository {
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

    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Resource
    IGroupBuyOrderDao groupBuyOrderDao;

    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId,
                                                                                                 String userId, Integer ownerCount) {
        GroupBuyOrderList request = new GroupBuyOrderList();
        request.setActivityId(activityId);
        request.setUserId(userId);
        request.setCount(ownerCount);
        return assembleTeams(groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByUserId(request));
    }


    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(Long activityId, String userId, Integer randomCount) {
        GroupBuyOrderList request = new GroupBuyOrderList();
        request.setActivityId(activityId);
        request.setUserId(userId);
        // 展示最多2条时，先查询4条候选，再打乱抽取。
        request.setCount(randomCount * 2);
        // DAO返回的候选复制后再打乱，避免修改调用方提供的列表。
        List<GroupBuyOrderList> rows = new ArrayList<>(
                groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByRandom(request));
        Collections.shuffle(rows);
        if (rows.size() > randomCount) rows = rows.subList(0, randomCount);
        return assembleTeams(rows);
    }

    private List<UserGroupBuyOrderDetailEntity> assembleTeams(List<GroupBuyOrderList> rows) {
        // 无候选就结束，否则批量SQL会出现IN ()。MyBatis的列表查询无结果返回空List。
        if (rows.isEmpty()) return Collections.emptyList();
        Set<String> ids = rows.stream().map(GroupBuyOrderList::getTeamId).collect(Collectors.toSet());
        Map<String, GroupBuyOrder> teams = groupBuyOrderDao.queryGroupBuyProgressByTeamIds(ids)
                .stream().collect(Collectors.toMap(GroupBuyOrder::getTeamId, team -> team));
        List<UserGroupBuyOrderDetailEntity> result = new ArrayList<>();
        for (GroupBuyOrderList row : rows) {
            GroupBuyOrder team = teams.get(row.getTeamId());
            // 两次查询间团可能已满或截止，以第二次查到的可展示团为准。
            if (team == null) continue;
            result.add(UserGroupBuyOrderDetailEntity.builder()
                    .userId(row.getUserId()).teamId(team.getTeamId()).activityId(team.getActivityId())
                    // SQL 已查到完成量，这里必须继续传给领域实体，首页才能返回。
                    .targetCount(team.getTargetCount())
                    .completeCount(team.getCompleteCount())
                    .lockCount(team.getLockCount()).validStartTime(team.getValidStartTime())
                    .validEndTime(team.getValidEndTime()).outTradeNo(row.getOutTradeNo())
                    .build());
        }
        return result;
    }

    public TeamStatisticVO queryTeamStatisticByActivityId(Long activityId) {
        return TeamStatisticVO.builder()
                .allTeamCount(groupBuyOrderDao.queryAllTeamCount(activityId))
                .allTeamCompleteCount(groupBuyOrderDao.queryAllTeamCompleteCount(activityId))
                .allTeamUserCount(groupBuyOrderDao.queryAllUserCount(activityId))
                .build();
    }

    public boolean isTagCrowdRange(String tagId, String userId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) return true;
        // 判断用户是否在人群中
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

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

    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (sku == null) return null;
        return SkuVO.builder()
                .goodsId(goodsId)
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();
    }

    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }
}
