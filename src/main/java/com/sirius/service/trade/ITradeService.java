package com.sirius.service.trade;

import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.vo.MarketPayOrderVO;
import com.sirius.model.dto.request.LockMarketPayOrderRequestDTO;
import com.sirius.model.dto.response.LockMarketPayOrderResponseDTO;
import com.sirius.model.dto.response.SettlementMarketPayOrderResponseDTO;
import com.sirius.model.dto.request.SettlementMarketPayOrderRequestDTO;

import java.util.Map;

public interface ITradeService {

    MarketPayOrderVO queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    LockMarketPayOrderResponseDTO lockMarketPayOrder(LockMarketPayOrderRequestDTO request)
            throws Exception;

    SettlementMarketPayOrderResponseDTO settlementMarketPayOrder(SettlementMarketPayOrderRequestDTO request)
            throws Exception;


    Map<String, Integer> execSettlementNotifyJob() throws Exception;

    Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception;
}
