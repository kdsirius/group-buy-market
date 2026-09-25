package com.sirius.domain.trade.adapter.repository;

import com.sirius.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.sirius.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.sirius.domain.trade.model.entity.GroupBuyActivityEntity;
import com.sirius.domain.trade.model.entity.GroupBuyTeamEntity;
import com.sirius.domain.trade.model.entity.MarketPayOrderEntity;
import com.sirius.domain.trade.model.valobj.GroupBuyProgressVO;

/**
 * @author Sirius
 * @description 交易仓储接口服务接口
 * @date 2026年09月22日 16:31
 */
public interface ITradeRepository {

    MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

}
