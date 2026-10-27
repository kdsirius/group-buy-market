package com.sirius.domain.trade.adapter.repository;

import com.sirius.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.sirius.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.sirius.domain.trade.model.entity.GroupBuyActivityEntity;
import com.sirius.domain.trade.model.entity.GroupBuyTeamEntity;
import com.sirius.domain.trade.model.entity.MarketPayOrderEntity;
import com.sirius.domain.trade.model.entity.NotifyTaskEntity;
import com.sirius.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.List;

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

    boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCBlackIntercept(String source, String channel);

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList();

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId);

    int updateNotifyTaskStatusSuccess(String teamId);

    int updateNotifyTaskStatusRetry(String teamId);

    int updateNotifyTaskStatusError(String teamId);

}
