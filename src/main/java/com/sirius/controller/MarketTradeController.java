package com.sirius.controller;

import com.sirius.model.dto.request.LockMarketPayOrderRequestDTO;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;
import com.sirius.model.dto.response.LockMarketPayOrderResponseDTO;
import com.sirius.model.dto.response.Response;
import com.sirius.model.dto.response.SettlementMarketPayOrderResponseDTO;
import com.sirius.model.dto.response.SettlementResult;
import com.sirius.service.trade.ITradeService;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController {

    @Resource
    private ITradeService tradeService;

    @PostMapping("lock_market_pay_order")
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(
            @RequestBody LockMarketPayOrderRequestDTO request) {
        try {
            LockMarketPayOrderResponseDTO result = tradeService.lockMarketPayOrder(request);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(result)
                    .build();
        } catch (AppException e) {
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("??????????", e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("settlement_market_pay_order")
    public Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(
            @RequestBody SettlementMarketPayOrderRequestDTO request) {
        try {
            SettlementResult result = tradeService.settlementMarketPayOrder(request);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(toResponse(result))
                    .build();
        } catch (AppException e) {
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("????????", e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private SettlementMarketPayOrderResponseDTO toResponse(SettlementResult result) {
        return SettlementMarketPayOrderResponseDTO.builder()
                .userId(result.getUserId())
                .teamId(result.getTeamId())
                .activityId(result.getActivityId())
                .outTradeNo(result.getOutTradeNo())
                .build();
    }
}
