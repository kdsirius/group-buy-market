package com.sirius.service.trade.impl;

import com.sirius.client.ITradePort;
import com.sirius.common.enums.ActivityStatusEnumVO;
import com.sirius.common.enums.NotifyTaskHTTPEnumVO;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import com.sirius.model.vo.GroupBuyActivityEntity;
import com.sirius.model.vo.GroupBuyTeamEntity;
import com.sirius.model.vo.MarketPayOrderEntity;
import com.sirius.model.vo.NotifyTaskEntity;
import com.sirius.model.dto.request.LockMarketPayOrderRequestDTO;
import com.sirius.model.dto.request.PayActivityEntity;
import com.sirius.model.dto.request.PayDiscountEntity;
import com.sirius.model.dto.response.LockMarketPayOrderResponseDTO;
import com.sirius.model.dto.response.TradePaySettlementEntity;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;
import com.sirius.model.dto.request.TradePaySuccessEntity;
import com.sirius.model.dto.request.UserEntity;
import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.dto.request.MarketProductEntity;
import com.sirius.model.vo.GroupBuyActivityDiscountVO;
import com.sirius.model.vo.TrialBalanceEntity;
import com.sirius.model.vo.NotifyConfigVO;
import com.sirius.model.enums.NotifyTypeEnumVO;
import com.sirius.service.market.IMarketService;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;
import com.sirius.repository.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TradeServiceImpl implements com.sirius.service.trade.ITradeService {

    @Resource
    private TradeRepository repository;

    @Resource
    private ITradePort port;

    @Resource
    private IMarketService marketService;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("????-?????????:{} outTradeNo:{}", userId, outTradeNo);
        return repository.queryMarketPayOrderEntityByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        log.info("????-??????:{}", teamId);
        return repository.queryGroupBuyProgress(teamId);
    }

    @Override
    public LockMarketPayOrderResponseDTO lockMarketPayOrder(LockMarketPayOrderRequestDTO request)
            throws Exception {
        validateLockRequest(request);

        MarketPayOrderEntity existing = queryNoPayMarketPayOrderByOutTradeNo(
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

        TrialBalanceEntity trial = marketService.indexMarketTrial(MarketProductEntity.builder()
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
        MarketPayOrderEntity result = lockMarketPayOrder(
                UserEntity.builder().userId(request.getUserId()).build(),
                PayActivityEntity.builder()
                        .teamId(request.getTeamId())
                        .activityId(request.getActivityId())
                        .activityName(discount.getActivityName())
                        .startTime(discount.getStartTime())
                        .endTime(discount.getEndTime())
                        .targetCount(discount.getTarget())
                        .validTime(discount.getValidTime())
                        .build(),
                PayDiscountEntity.builder()
                        .source(request.getSource())
                        .channel(request.getChannel())
                        .goodsId(request.getGoodsId())
                        .goodsName(trial.getGoodsName())
                        .originalPrice(trial.getOriginalPrice())
                        .deductionPrice(trial.getDeductionPrice())
                        .payPrice(trial.getPayPrice())
                        .outTradeNo(request.getOutTradeNo())
                        .notifyConfigVO(NotifyConfigVO.builder()
                                .notifyType(NotifyTypeEnumVO.valueOf(notify.getNotifyType()))
                                .notifyUrl(notify.getNotifyUrl())
                                .build())
                        .build());
        return toLockResponse(result);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(
            UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity)
            throws Exception {
        log.info("????-??????????:{} activityId:{} goodsId:{}",
                userEntity.getUserId(), payActivityEntity.getActivityId(), payDiscountEntity.getGoodsId());

        GroupBuyActivityEntity activity =
                repository.queryGroupBuyActivityEntityByActivityId(payActivityEntity.getActivityId());
        checkActivity(activity, payActivityEntity.getActivityId());

        Integer userTakeOrderCount = repository.queryOrderCountByActivityId(
                payActivityEntity.getActivityId(), userEntity.getUserId());
        if (activity.getTakeLimitCount() != null
                && userTakeOrderCount >= activity.getTakeLimitCount()) {
            throw new AppException(ResponseCode.E0103);
        }

        return repository.lockMarketPayOrder(
                userEntity, payActivityEntity, payDiscountEntity, userTakeOrderCount);
    }

    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(
            SettlementMarketPayOrderRequestDTO request) throws Exception {
        if (request == null || StringUtils.isBlank(request.getUserId())
                || StringUtils.isBlank(request.getSource())
                || StringUtils.isBlank(request.getChannel())
                || StringUtils.isBlank(request.getOutTradeNo())
                || request.getOutTradeTime() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }
        return settlementMarketPayOrder(TradePaySuccessEntity.builder()
                .source(request.getSource())
                .channel(request.getChannel())
                .userId(request.getUserId())
                .outTradeNo(request.getOutTradeNo())
                .outTradeTime(request.getOutTradeTime())
                .build());
    }

    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(
            TradePaySuccessEntity tradePaySuccessEntity) throws Exception {
        log.info("????-?????? userId:{} outTradeNo:{}",
                tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());

        if (repository.isSCBlackIntercept(
                tradePaySuccessEntity.getSource(), tradePaySuccessEntity.getChannel())) {
            throw new AppException(ResponseCode.E0105);
        }

        MarketPayOrderEntity order = repository.queryMarketPayOrderEntityByOutTradeNo(
                tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
        if (order == null) {
            throw new AppException(ResponseCode.E0104);
        }

        GroupBuyTeamEntity team = repository.queryGroupBuyTeamByTeamId(order.getTeamId());
        if (!tradePaySuccessEntity.getOutTradeTime().before(team.getValidEndTime())) {
            throw new AppException(ResponseCode.E0106);
        }

        boolean isNotify = repository.settlementMarketPayOrder(
                UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build(),
                team,
                tradePaySuccessEntity);
        if (isNotify) {
            execSettlementNotifyJob(team.getTeamId());
        }

        return TradePaySettlementEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build();
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob() throws Exception {
        return executeNotifyTasks(repository.queryUnExecutedNotifyTaskList());
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception {
        return executeNotifyTasks(repository.queryUnExecutedNotifyTaskList(teamId));
    }

    private Map<String, Integer> executeNotifyTasks(List<NotifyTaskEntity> tasks) throws Exception {
        int success = 0;
        int retry = 0;
        int error = 0;
        for (NotifyTaskEntity task : tasks) {
            String response = port.groupBuyNotify(task);
            if (NotifyTaskHTTPEnumVO.SUCCESS.getCode().equals(response)) {
                success += repository.updateNotifyTaskStatusSuccess(task.getTeamId());
            } else if (NotifyTaskHTTPEnumVO.ERROR.getCode().equals(response)) {
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

    private LockMarketPayOrderResponseDTO toLockResponse(MarketPayOrderEntity order) {
        return LockMarketPayOrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .originalPrice(order.getOriginalPrice())
                .deductionPrice(order.getDeductionPrice())
                .payPrice(order.getPayPrice())
                .tradeOrderStatus(order.getTradeOrderStatusEnumVO().getCode())
                .build();
    }

    private void checkActivity(GroupBuyActivityEntity activity, Long activityId) {
        if (!ActivityStatusEnumVO.EFFECTIVE.equals(activity.getStatus())) {
            throw new AppException(ResponseCode.E0101);
        }

        Date now = new Date();
        if (now.before(activity.getStartTime()) || now.after(activity.getEndTime())) {
            throw new AppException(ResponseCode.E0102);
        }
    }
}
