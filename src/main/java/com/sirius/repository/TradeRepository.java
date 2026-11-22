package com.sirius.repository;

import com.alibaba.fastjson.JSON;
import com.sirius.model.dto.request.*;
import com.sirius.model.dto.response.*;
import com.sirius.model.po.*;
import com.sirius.model.vo.*;
import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.vo.NotifyConfigVO;
import com.sirius.model.enums.NotifyType;
import com.sirius.model.enums.TradeOrderStatus;
import com.sirius.mapper.IGroupBuyActivityDao;
import com.sirius.mapper.IGroupBuyOrderDao;
import com.sirius.mapper.IGroupBuyOrderListDao;
import com.sirius.mapper.INotifyTaskDao;
import com.sirius.model.po.GroupBuyActivity;
import com.sirius.model.po.GroupBuyOrder;
import com.sirius.model.po.GroupBuyOrderList;
import com.sirius.model.po.NotifyTask;
import com.sirius.config.dcc.DCCService;
import com.sirius.common.Constants;
import com.sirius.common.enums.ActivityStatus;
import com.sirius.common.enums.GroupBuyOrderStatus;
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
 * @description 浜ゆ槗浠撳偍鏈嶅姟
 * @date 2026骞?9鏈?2鏃?16:32
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

    // 涓や釜鏌ヨ鍏辩敤鏄犲皠锛岄伩鍏嶅悓涓€瀛楁鍦ㄤ袱澶勬紡浼犮€?
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
    public MarketPayOrderVO lockMarketPayOrder(String userId, PayActivityEntity payActivityEntity,
                                                        PayDiscountEntity payDiscountEntity, Integer userTakeOrderCount) {
        NotifyConfigVO notifyConfigVO = payDiscountEntity.getNotifyConfigVO();

        // 鍒ゆ柇鏄惁鏈夊洟 - teamId涓虹┖ -> 鏂板洟銆乼eamId涓嶄负绌?-> 鑰佸洟
        String teamId = payActivityEntity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            // 鏇夸唬鍏徃浣跨敤鐨勯洩鑺盜D
            teamId = RandomStringUtils.randomNumeric(8);

            Date validStartTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(validStartTime);
            calendar.add(Calendar.MINUTE, payActivityEntity.getValidTime());
            Date validEndTime = calendar.getTime();


            // 鏋勫缓鎷煎洟璁㈠崟
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

            // 鍐欏叆璁板綍
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            // 濡傛灉涓嶇瓑浜?鍒欒〃绀烘嫾鍥㈠凡婊★紝鎶涘嚭寮傚父
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
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .status(TradeOrderStatus.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                // 鏋勫缓 bizId 鍞竴鍊硷紱娲诲姩id_鐢ㄦ埛id_鍙備笌娆℃暟澧炲姞
                .bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userId + Constants.UNDERLINE + (userTakeOrderCount + 1))
                .build();

        try {
            // 鍐欏叆鎷煎洟璁板綍
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (Exception e) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderVO.builder()
                .orderId(orderId)
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
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
    public boolean settlementMarketPayOrder(String userId, GroupBuyTeamVO groupBuyTeamEntity,
                                                      SettlementMarketPayOrderRequestDTO tradePaySuccessEntity) {
        NotifyConfigVO notifyConfigVO = groupBuyTeamEntity.getNotifyConfigVO();
        // 1. 鏇存柊鎷煎洟璁㈠崟鏄庣粏鐘舵€?
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(tradePaySuccessEntity.getOutTradeNo());
        groupBuyOrderListReq.setOutTradeTime(tradePaySuccessEntity.getOutTradeTime());
        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderListStatus2COMPLETE(groupBuyOrderListReq);
        if (1 != updateOrderListStatusCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 2. 鏇存柊鎷煎洟杈炬垚鏁伴噺
        int updateAddCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if (1 != updateAddCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 3. 鏇存柊鎷煎洟瀹屾垚鐘舵€?
        if (groupBuyTeamEntity.getTargetCount() - groupBuyTeamEntity.getCompleteCount() == 1) {
            int updateOrderStatusCount = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeamEntity.getTeamId());
            if (1 != updateOrderStatusCount) {
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            // 鏌ヨ鎷煎洟浜ゆ槗瀹屾垚澶栭儴鍗曞彿鍒楄〃
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeamEntity.getTeamId());

            // 鎷煎洟瀹屾垚鍐欏叆鍥炶皟浠诲姟璁板綍
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
            notifyTask.setNotifyMQ(NotifyType.MQ.equals(notifyConfigVO.getNotifyType())
                    ? notifyConfigVO.getNotifyMQ() : null);
            notifyTask.setNotifyUrl(NotifyType.HTTP.equals(notifyConfigVO.getNotifyType())
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
