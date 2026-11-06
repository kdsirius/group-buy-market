package com.sirius.trigger.http;

import com.alibaba.fastjson.JSON;
import com.sirius.api.IMarketTradeService;
import com.sirius.api.dto.LockMarketPayOrderRequestDTO;
import com.sirius.api.dto.LockMarketPayOrderResponseDTO;
import com.sirius.api.dto.SettlementMarketPayOrderRequestDTO;
import com.sirius.api.dto.SettlementMarketPayOrderResponseDTO;
import com.sirius.api.response.Response;
import com.sirius.domain.activity.model.entity.MarketProductEntity;
import com.sirius.domain.activity.model.entity.TrialBalanceEntity;
import com.sirius.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.sirius.domain.activity.service.IIndexGroupBuyMarketService;
import com.sirius.domain.trade.model.entity.*;
import com.sirius.domain.trade.model.valobj.GroupBuyProgressVO;
import com.sirius.domain.trade.service.ITradeLockOrderService;
import com.sirius.domain.trade.service.ITradeSettlementOrderService;
import com.sirius.types.enums.ResponseCode;
import com.sirius.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author Sirius
 * @description 营销交易服务
 * @date 2026年09月22日 19:58
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController implements IMarketTradeService {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeLockOrderService tradeOrderService;

    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;


    /**
     * 拼团营销锁单
     */
    @PostMapping("lock_market_pay_order")
    @Override
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(@RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {
        try {
            // 获取参数
            String userId = lockMarketPayOrderRequestDTO.getUserId();
            String teamId = lockMarketPayOrderRequestDTO.getTeamId();
            Long activityId = lockMarketPayOrderRequestDTO.getActivityId();
            String goodsId = lockMarketPayOrderRequestDTO.getGoodsId();
            String source = lockMarketPayOrderRequestDTO.getSource();
            String channel = lockMarketPayOrderRequestDTO.getChannel();
            String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo();
            String notifyUrl = lockMarketPayOrderRequestDTO.getNotifyUrl();

            log.info("营销交易锁单:{} LockMarketPayOrderRequestDTO:{}", userId, JSON.toJSONString(lockMarketPayOrderRequestDTO));

            // 校验参数是否为空
            if (StringUtils.isBlank(userId) || StringUtils.isBlank(source) || StringUtils.isBlank(outTradeNo)
                    || StringUtils.isBlank(channel) || StringUtils.isBlank(goodsId) || null == activityId) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 查询 outTradeNo 是否已经存在交易记录
            MarketPayOrderEntity marketPayOrderEntity = tradeOrderService.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
            if (null != marketPayOrderEntity) {
                LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = LockMarketPayOrderResponseDTO.builder()
                        .orderId(marketPayOrderEntity.getOrderId())
                        .originalPrice(marketPayOrderEntity.getOriginalPrice())
                        .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                        .payPrice(marketPayOrderEntity.getPayPrice())
                        .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                        .build();

                log.info("交易锁单记录(存在):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(lockMarketPayOrderResponseDTO)
                        .build();
            }

            // 判断拼团接单是否实现了目标
            if (null != teamId) {
                GroupBuyProgressVO groupBuyProgressVO = tradeOrderService.queryGroupBuyProgress(teamId);
                if (null != groupBuyProgressVO && Objects.equals(groupBuyProgressVO.getTargetCount(), groupBuyProgressVO.getLockCount())) {
                    log.info("交易锁单拦截-拼单目标已达成:{} {}", userId, teamId);
                    return Response.<LockMarketPayOrderResponseDTO>builder()
                            .code(ResponseCode.E0006.getCode())
                            .info(ResponseCode.E0006.getInfo())
                            .build();
                }
            }

            // 营销优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .activityId(activityId)
                    .userId(userId)
                    .goodsId(goodsId)
                    .source(source)
                    .channel(channel)
                    .build());

            // 人群限定
            if (!trialBalanceEntity.getIsVisible() || !trialBalanceEntity.getIsEnable()) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.E0007.getCode())
                        .info(ResponseCode.E0007.getInfo())
                        .build();
            }

            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

            // 锁单
            marketPayOrderEntity = tradeOrderService.lockMarketPayOrder(
                    UserEntity.builder().userId(userId).build(),
                    PayActivityEntity.builder()
                            .teamId(teamId)
                            .activityId(activityId)
                            .activityName(groupBuyActivityDiscountVO.getActivityName())
                            .startTime(groupBuyActivityDiscountVO.getStartTime())
                            .endTime(groupBuyActivityDiscountVO.getEndTime())
                            .targetCount(groupBuyActivityDiscountVO.getTarget())
                            .validTime(groupBuyActivityDiscountVO.getValidTime())
                            .build(),
                    PayDiscountEntity.builder()
                            .source(source)
                            .channel(channel)
                            .goodsId(goodsId)
                            .goodsName(trialBalanceEntity.getGoodsName())
                            .originalPrice(trialBalanceEntity.getOriginalPrice())
                            .deductionPrice(trialBalanceEntity.getDeductionPrice())
                            .payPrice(trialBalanceEntity.getPayPrice())
                            .outTradeNo(outTradeNo)
                            .notifyUrl(notifyUrl)
                            .build()
            );
            log.info("交易锁单记录(新):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));

            // 返回结果
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(LockMarketPayOrderResponseDTO.builder()
                            .orderId(marketPayOrderEntity.getOrderId())
                            .originalPrice(marketPayOrderEntity.getOriginalPrice())
                            .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                            .payPrice(marketPayOrderEntity.getPayPrice())
                            .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                            .build())
                    .build();
        } catch (AppException e) {
            log.error("营销交易锁单业务异常：{} LockMarketPayOrderRequestDTO{}", lockMarketPayOrderRequestDTO.getUserId(),
                    JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销交易锁单服务失败：{} LockMarketPayOrderRequestDTO{}", lockMarketPayOrderRequestDTO.getUserId(),
                    JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 拼团结算
     */
    @PostMapping("settlement_market_pay_order")
    @Override
    public Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(
            @RequestBody SettlementMarketPayOrderRequestDTO request) {
        if (StringUtils.isAnyBlank(request.getUserId(), request.getSource(), request.getChannel(),
                request.getOutTradeNo()) || request.getOutTradeTime() == null) {
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        try {
            // Controller只转换入参；时间与订单规则仍交给已有领域服务。
            TradePaySettlementEntity result = tradeSettlementOrderService.settlementMarketPayOrder(toTradePaySuccessEntity(request));
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(toSettlementMarketPayOrderResponseDTO(result))
                    .build();
        } catch (AppException e) {
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("模拟结算接口失败", e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode()).info(ResponseCode.UN_ERROR.getInfo()).build();
        }
    }

    private SettlementMarketPayOrderResponseDTO toSettlementMarketPayOrderResponseDTO(TradePaySettlementEntity result) {
        return SettlementMarketPayOrderResponseDTO.builder()
                .userId(result.getUserId())
                .teamId(result.getTeamId())
                .activityId(result.getActivityId())
                .outTradeNo(result.getOutTradeNo())
                .build();
    }

    private TradePaySuccessEntity toTradePaySuccessEntity(SettlementMarketPayOrderRequestDTO request) {
        return TradePaySuccessEntity.builder()
                .source(request.getSource())
                .channel(request.getChannel())
                .userId(request.getUserId())
                .outTradeNo(request.getOutTradeNo())
                .outTradeTime(request.getOutTradeTime())
                .build();
    }

}
