package com.sirius.service.trade;

import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.vo.MarketPayOrderEntity;
import com.sirius.model.dto.request.LockMarketPayOrderRequestDTO;
import com.sirius.model.dto.request.PayActivityEntity;
import com.sirius.model.dto.request.PayDiscountEntity;
import com.sirius.model.dto.response.LockMarketPayOrderResponseDTO;
import com.sirius.model.dto.response.TradePaySettlementEntity;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;

import java.util.Map;

public interface ITradeService {

    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    LockMarketPayOrderResponseDTO lockMarketPayOrder(LockMarketPayOrderRequestDTO request)
            throws Exception;

    MarketPayOrderEntity lockMarketPayOrder(
            String userId, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity)
            throws Exception;

    TradePaySettlementEntity settlementMarketPayOrder(SettlementMarketPayOrderRequestDTO request)
            throws Exception;


    Map<String, Integer> execSettlementNotifyJob() throws Exception;

    Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception;
}
