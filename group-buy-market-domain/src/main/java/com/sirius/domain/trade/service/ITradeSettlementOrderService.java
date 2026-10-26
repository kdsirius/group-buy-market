package com.sirius.domain.trade.service;

import com.sirius.domain.trade.model.entity.TradePaySettlementEntity;
import com.sirius.domain.trade.model.entity.TradePaySuccessEntity;

/**
 * @author Sirius
 * @description 拼团交易结算服务接口
 * @date 2026年09月25日 11:16
 */
public interface ITradeSettlementOrderService {


    /**
     * 营销结算
     *
     * @param tradePaySuccessEntity 交易支付订单实体对象
     * @return 交易结算订单实体
     */
    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;


}
