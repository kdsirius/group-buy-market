package com.sirius.service.trade;

import com.sirius.model.vo.GroupBuyProgressVO;
import com.sirius.model.vo.MarketPayOrderEntity;
import com.sirius.model.dto.request.PayActivityEntity;
import com.sirius.model.dto.request.PayDiscountEntity;
import com.sirius.model.dto.response.TradePaySettlementEntity;
import com.sirius.model.dto.request.TradePaySuccessEntity;
import com.sirius.model.dto.request.UserEntity;

import java.util.Map;

public interface ITradeService {

    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    MarketPayOrderEntity lockMarketPayOrder(
            UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity)
            throws Exception;

    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity)
            throws Exception;

    Map<String, Integer> execSettlementNotifyJob() throws Exception;

    Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception;
}
