package com.sirius.repository;

import com.alibaba.fastjson.JSON;
import com.sirius.common.Constants;
import com.sirius.common.enums.ActivityStatus;
import com.sirius.common.enums.GroupBuyOrderStatus;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import com.sirius.config.dcc.DCCService;
import com.sirius.mapper.IGroupBuyActivityDao;
import com.sirius.mapper.IGroupBuyOrderDao;
import com.sirius.mapper.IGroupBuyOrderListDao;
import com.sirius.mapper.INotifyTaskDao;
import com.sirius.model.PayActivity;
import com.sirius.model.PayDiscount;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;
import com.sirius.model.enums.NotifyType;
import com.sirius.model.enums.TradeOrderStatus;
import com.sirius.model.po.GroupBuyActivity;
import com.sirius.model.po.GroupBuyOrder;
import com.sirius.model.po.GroupBuyOrderList;
import com.sirius.model.po.NotifyTask;
import com.sirius.model.vo.GroupBuyActivityVO;
import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.vo.GroupBuyTeamVO;
import com.sirius.model.vo.MarketPayOrderVO;
import com.sirius.model.vo.NotifyConfigVO;
import com.sirius.model.vo.NotifyTaskVO;
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

    public List<NotifyTaskVO> queryUnExecutedNotifyTaskList() {
        return toNotifyEntities(notifyTaskDao.queryUnExecutedNotifyTaskList());
    }

    public List<NotifyTaskVO> queryUnExecutedNotifyTaskList(String teamId) {
        return toNotifyEntities(notifyTaskDao.queryUnExecutedNotifyTaskByTeamId(teamId));
    }

    // 两个查询共用映射，避免同一字段在两处漏传。
    private List<NotifyTaskVO> toNotifyEntities(List<NotifyTask> tasks) {
        return tasks.stream()
                .map(task -> NotifyTaskVO.builder()
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

    public MarketPayOrderVO queryMarketPayOrderVOByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList req = new GroupBuyOrderList();
        req.setUserId(userId);
        req.setOutTradeNo(outTradeNo);
        GroupBuyOrderList record = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(req);
        if (null == record) return null;
        return MarketPayOrderVO.builder()
                .teamId(record.getTeamId())
                .orderId(record.getOrderId())
                .originalPrice(record.getOriginalPrice())
                .deductionPrice(record.getDeductionPrice())
                .payPrice(record.getPayPrice())
                .tradeOrderStatus(TradeOrderStatus.valueOf(record.getStatus()))
                .build();
    }

    @Transactional(timeout = 500)
    public MarketPayOrderVO lockMarketPayOrder(String userId, PayActivity payActivity,
                                               PayDiscount payDiscount, Integer userTakeOrderCount) {
        NotifyConfigVO notifyConfigVO = payDiscount.getNotifyConfigVO();

        // 判断是否有团 - teamId为空 -> 新团、teamId不为空 -> 老团
        String teamId = payActivity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            // 替代公司使用的雪花ID
            teamId = RandomStringUtils.randomNumeric(8);

            Date validStartTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(validStartTime);
            calendar.add(Calendar.MINUTE, payActivity.getValidTime());
            Date validEndTime = calendar.getTime();

            // 构建拼团订单
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivity.getActivityId())
                    .source(payDiscount.getSource())
                    .channel(payDiscount.getChannel())
                    .originalPrice(payDiscount.getOriginalPrice())
                    .deductionPrice(payDiscount.getDeductionPrice())
                    .payPrice(payDiscount.getPayPrice())
                    .targetCount(payActivity.getTargetCount())
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
                .userId(userId)
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivity.getActivityId())
                .startTime(payActivity.getStartTime())
                .endTime(payActivity.getEndTime())
                .goodsId(payDiscount.getGoodsId())
                .source(payDiscount.getSource())
                .channel(payDiscount.getChannel())
                .originalPrice(payDiscount.getOriginalPrice())
                .deductionPrice(payDiscount.getDeductionPrice())
                .payPrice(payDiscount.getPayPrice())
                .status(TradeOrderStatus.CREATE.getCode())
                .outTradeNo(payDiscount.getOutTradeNo())
                // 构建 bizId 唯一值；活动id_用户id_参与次数增加
                .bizId(payActivity.getActivityId() + Constants.UNDERLINE + userId + Constants.UNDERLINE + (userTakeOrderCount + 1))
                .build();

        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (Exception e) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderVO.builder()
                .orderId(orderId)
                .originalPrice(payDiscount.getOriginalPrice())
                .deductionPrice(payDiscount.getDeductionPrice())
                .payPrice(payDiscount.getPayPrice())
                .tradeOrderStatus(TradeOrderStatus.CREATE)
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

    public GroupBuyActivityVO queryGroupBuyActivityVOByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        if (null == groupBuyActivity) return null;
        return GroupBuyActivityVO.builder()
                .activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName())
                .discountId(groupBuyActivity.getDiscountId())
                .groupType(groupBuyActivity.getGroupType())
                .takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget())
                .validTime(groupBuyActivity.getValidTime())
                .status(ActivityStatus.valueOf(groupBuyActivity.getStatus()))
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

    public GroupBuyTeamVO queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        return GroupBuyTeamVO.builder()
                .teamId(groupBuyOrder.getTeamId())
                .activityId(groupBuyOrder.getActivityId())
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .status(GroupBuyOrderStatus.valueOf(groupBuyOrder.getStatus()))
                .validStartTime(groupBuyOrder.getValidStartTime())
                .validEndTime(groupBuyOrder.getValidEndTime())
                .notifyConfigVO(NotifyConfigVO.builder()
                        .notifyType(NotifyType.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyMQ(topicTeamSuccess)
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        .build())
                .build();
    }

    @Transactional(timeout = 500)
    public boolean settlementMarketPayOrder(String userId, GroupBuyTeamVO groupBuyTeam,
                                            SettlementMarketPayOrderRequestDTO request) {
        NotifyConfigVO notifyConfigVO = groupBuyTeam.getNotifyConfigVO();
        // 1. 更新拼团订单明细状态
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(request.getOutTradeNo());
        groupBuyOrderListReq.setOutTradeTime(request.getOutTradeTime());
        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderListStatus2COMPLETE(groupBuyOrderListReq);
        if (1 != updateOrderListStatusCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 2. 更新拼团达成数量
        int updateAddCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeam.getTeamId());
        if (1 != updateAddCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 3. 更新拼团完成状态
        if (groupBuyTeam.getTargetCount() - groupBuyTeam.getCompleteCount() == 1) {
            int updateOrderStatusCount = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeam.getTeamId());
            if (1 != updateOrderStatusCount) {
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            // 查询拼团交易完成外部单号列表
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeam.getTeamId());

            // 拼团完成写入回调任务记录
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeam.getActivityId());
            notifyTask.setTeamId(groupBuyTeam.getTeamId());
            notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
            notifyTask.setNotifyMQ(NotifyType.MQ.equals(notifyConfigVO.getNotifyType())
                    ? notifyConfigVO.getNotifyMQ() : null);
            notifyTask.setNotifyUrl(NotifyType.HTTP.equals(notifyConfigVO.getNotifyType())
                    ? notifyConfigVO.getNotifyUrl() : null);
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeam.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));
            notifyTaskDao.insert(notifyTask);
            return true;
        }
        return false;
    }
}
