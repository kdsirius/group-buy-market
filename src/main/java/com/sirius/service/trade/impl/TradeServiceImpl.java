package com.sirius.service.trade.impl;

import com.sirius.client.IGroupBuyNotificationClient;
import com.sirius.common.enums.ActivityStatus;
import com.sirius.common.enums.NotifyResult;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import com.sirius.model.PayActivity;
import com.sirius.model.PayDiscount;
import com.sirius.model.dto.request.LockMarketPayOrderRequestDTO;
import com.sirius.model.dto.request.MarketProductRequest;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;
import com.sirius.model.dto.response.LockMarketPayOrderResponseDTO;
import com.sirius.model.dto.response.SettlementMarketPayOrderResponseDTO;
import com.sirius.model.enums.NotifyType;
import com.sirius.model.vo.GroupBuyActivityDiscountVO;
import com.sirius.model.vo.GroupBuyActivityVO;
import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.vo.GroupBuyTeamVO;
import com.sirius.model.vo.MarketPayOrderVO;
import com.sirius.model.vo.NotifyConfigVO;
import com.sirius.model.vo.NotifyTaskVO;
import com.sirius.model.vo.TrialBalanceVO;
import com.sirius.repository.TradeRepository;
import com.sirius.service.market.IMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class TradeServiceImpl implements com.sirius.service.trade.ITradeService {

    @Resource
    private TradeRepository repository;

    @Resource
    private IGroupBuyNotificationClient notificationClient;

    @Resource
    private IMarketService marketService;

    private MarketPayOrderVO queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("拼团交易-查询未支付营销订单:{} outTradeNo:{}", userId, outTradeNo);
        return repository.queryMarketPayOrderVOByOutTradeNo(userId, outTradeNo);
    }

    private GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        log.info("拼团交易-查询拼单进度:{}", teamId);
        return repository.queryGroupBuyProgress(teamId);
    }

    @Override
    public LockMarketPayOrderResponseDTO lockMarketPayOrder(LockMarketPayOrderRequestDTO request)
            throws Exception {
        validateLockRequest(request);

        MarketPayOrderVO existing = queryNoPayMarketPayOrderByOutTradeNo(
                request.getUserId(), request.getOutTradeNo());
        if (existing != null) {
            return toLockResponse(existing);
        }

        if (request.getTeamId() != null) {
            GroupBuyProgressVO progress = queryGroupBuyProgress(request.getTeamId());
            if (progress != null && Objects.equals(progress.getTargetCount(), progress.getLockCount())) {
                throw new AppException(ResponseCode.E0006);
            }
        }

        TrialBalanceVO trial = marketService.indexMarketTrial(MarketProductRequest.builder()
                .activityId(request.getActivityId())
                .userId(request.getUserId())
                .goodsId(request.getGoodsId())
                .source(request.getSource())
                .channel(request.getChannel())
                .build());
        if (!Boolean.TRUE.equals(trial.getIsVisible()) || !Boolean.TRUE.equals(trial.getIsEnable())) {
            throw new AppException(ResponseCode.E0007);
        }

        LockMarketPayOrderRequestDTO.NotifyConfigVO notify = request.getNotifyConfigVO();
        if (notify == null || StringUtils.isBlank(notify.getNotifyType())
                || (!"HTTP".equals(notify.getNotifyType()) && !"MQ".equals(notify.getNotifyType()))
                || ("HTTP".equals(notify.getNotifyType()) && StringUtils.isBlank(notify.getNotifyUrl()))) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        GroupBuyActivityDiscountVO discount = trial.getGroupBuyActivityDiscountVO();
        MarketPayOrderVO result = lockMarketPayOrder(
                request.getUserId(),
                PayActivity.builder()
                        .teamId(request.getTeamId())
                        .activityId(request.getActivityId())
                        .activityName(discount.getActivityName())
                        .startTime(discount.getStartTime())
                        .endTime(discount.getEndTime())
                        .targetCount(discount.getTarget())
                        .validTime(discount.getValidTime())
                        .build(),
                PayDiscount.builder()
                        .source(request.getSource())
                        .channel(request.getChannel())
                        .goodsId(request.getGoodsId())
                        .goodsName(trial.getGoodsName())
                        .originalPrice(trial.getOriginalPrice())
                        .deductionPrice(trial.getDeductionPrice())
                        .payPrice(trial.getPayPrice())
                        .outTradeNo(request.getOutTradeNo())
                        .notifyConfigVO(NotifyConfigVO.builder()
                                .notifyType(NotifyType.valueOf(notify.getNotifyType()))
                                .notifyUrl(notify.getNotifyUrl())
                                .build())
                        .build());
        return toLockResponse(result);
    }

    private MarketPayOrderVO lockMarketPayOrder(
            String userId, PayActivity payActivity, PayDiscount payDiscount)
            throws Exception {
        log.info("拼团交易-锁定营销优惠支付订单:{} activityId:{} goodsId:{}",
                userId, payActivity.getActivityId(), payDiscount.getGoodsId());

        GroupBuyActivityVO activity =
                repository.queryGroupBuyActivityVOByActivityId(payActivity.getActivityId());
        checkActivity(activity, payActivity.getActivityId());

        Integer userTakeOrderCount = repository.queryOrderCountByActivityId(
                payActivity.getActivityId(), userId);
        if (activity.getTakeLimitCount() != null
                && userTakeOrderCount >= activity.getTakeLimitCount()) {
            throw new AppException(ResponseCode.E0103);
        }

        return repository.lockMarketPayOrder(
                userId, payActivity, payDiscount, userTakeOrderCount);
    }

    @Override
    public SettlementMarketPayOrderResponseDTO settlementMarketPayOrder(
            SettlementMarketPayOrderRequestDTO request) throws Exception {
        if (request == null || StringUtils.isBlank(request.getUserId())
                || StringUtils.isBlank(request.getSource())
                || StringUtils.isBlank(request.getChannel())
                || StringUtils.isBlank(request.getOutTradeNo())
                || request.getOutTradeTime() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        log.info("拼团交易-交易订单结算 userId:{} outTradeNo:{}",
                request.getUserId(), request.getOutTradeNo());

        if (repository.isSCBlackIntercept(request.getSource(), request.getChannel())) {
            throw new AppException(ResponseCode.E0105);
        }

        MarketPayOrderVO order = repository.queryMarketPayOrderVOByOutTradeNo(
                request.getUserId(), request.getOutTradeNo());
        if (order == null) {
            throw new AppException(ResponseCode.E0104);
        }

        GroupBuyTeamVO team = repository.queryGroupBuyTeamByTeamId(order.getTeamId());
        if (!request.getOutTradeTime().before(team.getValidEndTime())) {
            throw new AppException(ResponseCode.E0106);
        }

        boolean isNotify = repository.settlementMarketPayOrder(
                request.getUserId(), team, request);
        if (isNotify) {
            execSettlementNotifyJob(team.getTeamId());
        }

        return SettlementMarketPayOrderResponseDTO.builder()
                .userId(request.getUserId())
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .outTradeNo(request.getOutTradeNo())
                .build();
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob() throws Exception {
        return executeNotifyTasks(repository.queryUnExecutedNotifyTaskList());
    }

    private Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception {
        return executeNotifyTasks(repository.queryUnExecutedNotifyTaskList(teamId));
    }

    private Map<String, Integer> executeNotifyTasks(List<NotifyTaskVO> tasks) throws Exception {
        int success = 0;
        int retry = 0;
        int error = 0;
        for (NotifyTaskVO task : tasks) {
            String response = notificationClient.groupBuyNotify(task);
            if (NotifyResult.SUCCESS.getCode().equals(response)) {
                success += repository.updateNotifyTaskStatusSuccess(task.getTeamId());
            } else if (NotifyResult.ERROR.getCode().equals(response)) {
                if (task.getNotifyCount() + 1 >= 5) {
                    error += repository.updateNotifyTaskStatusError(task.getTeamId());
                } else {
                    retry += repository.updateNotifyTaskStatusRetry(task.getTeamId());
                }
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("waitCount", tasks.size());
        result.put("successCount", success);
        result.put("retryCount", retry);
        result.put("errorCount", error);
        return result;
    }

    private void validateLockRequest(LockMarketPayOrderRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getUserId())
                || StringUtils.isBlank(request.getSource())
                || StringUtils.isBlank(request.getOutTradeNo())
                || StringUtils.isBlank(request.getChannel())
                || StringUtils.isBlank(request.getGoodsId())
                || request.getActivityId() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
    }

    private LockMarketPayOrderResponseDTO toLockResponse(MarketPayOrderVO order) {
        return LockMarketPayOrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .originalPrice(order.getOriginalPrice())
                .deductionPrice(order.getDeductionPrice())
                .payPrice(order.getPayPrice())
                .tradeOrderStatus(order.getTradeOrderStatus().getCode())
                .build();
    }

    private void checkActivity(GroupBuyActivityVO activity, Long activityId) {
        if (!ActivityStatus.EFFECTIVE.equals(activity.getStatus())) {
            throw new AppException(ResponseCode.E0101);
        }

        Date now = new Date();
        if (now.before(activity.getStartTime()) || now.after(activity.getEndTime())) {
            throw new AppException(ResponseCode.E0102);
        }
    }
}
