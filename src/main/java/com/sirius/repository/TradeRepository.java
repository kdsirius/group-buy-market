package com.sirius.repository;

import com.alibaba.fastjson.JSON;
import com.sirius.model.aggregate.GroupBuyOrderAggregate;
import com.sirius.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.sirius.model.entity.*;
import com.sirius.model.value.GroupBuyProgressVO;
import com.sirius.model.value.NotifyConfigVO;
import com.sirius.model.value.NotifyTypeEnumVO;
import com.sirius.model.value.TradeOrderStatusEnumVO;
import com.sirius.mapper.IGroupBuyActivityDao;
import com.sirius.mapper.IGroupBuyOrderDao;
import com.sirius.mapper.IGroupBuyOrderListDao;
import com.sirius.mapper.INotifyTaskDao;
import com.sirius.model.entity.GroupBuyActivity;
import com.sirius.model.entity.GroupBuyOrder;
import com.sirius.model.entity.GroupBuyOrderList;
import com.sirius.model.entity.NotifyTask;
import com.sirius.config.dcc.DCCService;
import com.sirius.common.Constants;
import com.sirius.common.enums.ActivityStatusEnumVO;
import com.sirius.common.enums.GroupBuyOrderEnumVO;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sirius
 * @description 交易仓储服务
 * @date 2026年09月22日 16:32
 */
@Repository
public class TradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;

    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private INotifyTaskDao notifyTaskDao;

    @Resource
    private DCCService dccService;

    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String topicTeamSuccess;

    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        return toNotifyEntities(notifyTaskDao.queryUnExecutedNotifyTaskList());
    }

    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId) {
        return toNotifyEntities(notifyTaskDao.queryUnExecutedNotifyTaskByTeamId(teamId));
    }

    // 两个查询共用映射，避免同一字段在两处漏传。
    private List<NotifyTaskEntity> toNotifyEntities(List<NotifyTask> tasks) {
        return tasks.stream()
                .map(task -> NotifyTaskEntity.builder()
                        .teamId(task.getTeamId())
                        .notifyType(task.getNotifyType())
                        .notifyMQ(task.getNotifyMQ())
                        .notifyUrl(task.getNotifyUrl())
                        .notifyCount(task.getNotifyCount())
                        .parameterJson(task.getParameterJson())
                        .build())
                .collect(Collectors.toList());
    }

    public int updateNotifyTaskStatusSuccess(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusSuccess(teamId);
    }

    public int updateNotifyTaskStatusRetry(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusRetry(teamId);
    }

    public int updateNotifyTaskStatusError(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusError(teamId);
    }

    public boolean isSCBlackIntercept(String source, String channel) {
        return dccService.isSCBlackIntercept(source, channel);
    }

    public MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList req = new GroupBuyOrderList();
        req.setUserId(userId);
        req.setOutTradeNo(outTradeNo);
        GroupBuyOrderList record = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(req);
        if (null == record) return null;
        return MarketPayOrderEntity.builder()
                .teamId(record.getTeamId())
                .orderId(record.getOrderId())
                .originalPrice(record.getOriginalPrice())
                .deductionPrice(record.getDeductionPrice())
                .payPrice(record.getPayPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(record.getStatus()))
                .build();
    }

    @Transactional(timeout = 500)
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {
        // 获取聚合信息
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        NotifyConfigVO notifyConfigVO = payDiscountEntity.getNotifyConfigVO();
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();

        // 判断是否有团 - teamId为空 -> 新团、teamId不为空 -> 老团
        String teamId = payActivityEntity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            // 替代公司使用的雪花ID
            teamId = RandomStringUtils.randomNumeric(8);

            Date validStartTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(validStartTime);
            calendar.add(Calendar.MINUTE, payActivityEntity.getValidTime());
            Date validEndTime = calendar.getTime();


            // 构建拼团订单
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice())
                    .payPrice(payDiscountEntity.getPayPrice())
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .lockCount(1)
                    .validStartTime(validStartTime)
                    .validEndTime(validEndTime)
                    .notifyType(notifyConfigVO.getNotifyType().getCode())
                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                    .build();

            // 写入记录
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            // 如果不等于1则表示拼团已满，抛出异常
            int updateAddTargetCount = groupBuyOrderDao.updateAddLockCount(teamId);
            if (1 != updateAddTargetCount) {
                throw new AppException(ResponseCode.E0005);
            }
        }
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderList = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                // 构建 bizId 唯一值；活动id_用户id_参与次数增加
                .bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE + (userTakeOrderCount + 1))
                .build();

        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (Exception e) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
                .build();
    }

    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if (null == groupBuyOrder) return null;
        return GroupBuyProgressVO.builder()
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .build();
    }

    public GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        if (null == groupBuyActivity) return null;
        return GroupBuyActivityEntity.builder()
                .activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName())
                .discountId(groupBuyActivity.getDiscountId())
                .groupType(groupBuyActivity.getGroupType())
                .takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget())
                .validTime(groupBuyActivity.getValidTime())
                .status(ActivityStatusEnumVO.valueOf(groupBuyActivity.getStatus()))
                .startTime(groupBuyActivity.getStartTime())
                .endTime(groupBuyActivity.getEndTime())
                .tagId(groupBuyActivity.getTagId())
                .tagScope(groupBuyActivity.getTagScope())
                .build();
    }

    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .activityId(activityId)
                .userId(userId)
                .build();
        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);
    }

    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        return GroupBuyTeamEntity.builder()
                .teamId(groupBuyOrder.getTeamId())
                .activityId(groupBuyOrder.getActivityId())
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .status(GroupBuyOrderEnumVO.valueOf(groupBuyOrder.getStatus()))
                .validStartTime(groupBuyOrder.getValidStartTime())
                .validEndTime(groupBuyOrder.getValidEndTime())
                .notifyConfigVO(NotifyConfigVO.builder()
                        .notifyType(NotifyTypeEnumVO.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyMQ(topicTeamSuccess)
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        .build())
                .build();
    }

    @Transactional(timeout = 500)
    public boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {
        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        NotifyConfigVO notifyConfigVO = groupBuyTeamEntity.getNotifyConfigVO();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();
        // 1. 更新拼团订单明细状态
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userEntity.getUserId());
        groupBuyOrderListReq.setOutTradeNo(tradePaySuccessEntity.getOutTradeNo());
        groupBuyOrderListReq.setOutTradeTime(tradePaySuccessEntity.getOutTradeTime());
        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderListStatus2COMPLETE(groupBuyOrderListReq);
        if (1 != updateOrderListStatusCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 2. 更新拼团达成数量
        int updateAddCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if (1 != updateAddCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 3. 更新拼团完成状态
        if (groupBuyTeamEntity.getTargetCount() - groupBuyTeamEntity.getCompleteCount() == 1) {
            int updateOrderStatusCount = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeamEntity.getTeamId());
            if (1 != updateOrderStatusCount) {
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            // 查询拼团交易完成外部单号列表
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeamEntity.getTeamId());

            // 拼团完成写入回调任务记录
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
            notifyTask.setNotifyMQ(NotifyTypeEnumVO.MQ.equals(notifyConfigVO.getNotifyType())
                    ? notifyConfigVO.getNotifyMQ() : null);
            notifyTask.setNotifyUrl(NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType())
                    ? notifyConfigVO.getNotifyUrl() : null);
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));
            notifyTaskDao.insert(notifyTask);
            return true;
        }
        return false;
    }
}
