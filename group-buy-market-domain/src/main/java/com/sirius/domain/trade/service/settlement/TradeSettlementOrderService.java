package com.sirius.domain.trade.service.settlement;

import com.sirius.domain.trade.adapter.port.ITradePort;
import com.sirius.domain.trade.adapter.repository.ITradeRepository;
import com.sirius.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.sirius.domain.trade.model.entity.*;
import com.sirius.domain.trade.service.ITradeSettlementOrderService;
import com.sirius.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.types.design.framework.link.mode2.chain.BusinessLinkedList;
import com.sirius.types.enums.NotifyTaskHTTPEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sirius
 * @description 拼团交易结算服务
 * @date 2026年09月25日 11:20
 */
@Slf4j
@Service
public class TradeSettlementOrderService implements ITradeSettlementOrderService {
    @Resource
    private ITradeRepository repository;

    @Resource
    private ITradePort port;

    @Resource
    private BusinessLinkedList<TradeSettlementRuleCommandEntity,
            TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter;

    @Override
    public Map<String, Integer> execSettlementNotifyJob() throws Exception {
        return executeNotifyTasks(repository.queryUnExecutedNotifyTaskList());
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception {
        return executeNotifyTasks(repository.queryUnExecutedNotifyTaskList(teamId));
    }

    private Map<String, Integer> executeNotifyTasks(List<NotifyTaskEntity> tasks) throws Exception {
        int success = 0, retry = 0, error = 0;
        for (NotifyTaskEntity task : tasks) {
            String response = port.groupBuyNotify(task);
            if (NotifyTaskHTTPEnumVO.SUCCESS.getCode().equals(response)) {
                success += repository.updateNotifyTaskStatusSuccess(task.getTeamId());
            } else if (NotifyTaskHTTPEnumVO.ERROR.getCode().equals(response)) {
                // notifyCount 是已经尝试的数量，加1才是“本次是第几次”。
                if (task.getNotifyCount() + 1 >= 5) {
                    error += repository.updateNotifyTaskStatusError(task.getTeamId());
                } else {
                    retry += repository.updateNotifyTaskStatusRetry(task.getTeamId());
                }
            }
            // null标记表示没抢到锁：不发送、不增加次数，下一轮再查。
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("waitCount", tasks.size());
        result.put("successCount", success);
        result.put("retryCount", retry);
        result.put("errorCount", error);
        return result;
    }

    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {
        log.info("拼团交易-支付订单结算 userId:{} outTradeNo:{}",
                tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());

        TradeSettlementRuleFilterBackEntity back = tradeSettlementRuleFilter.apply(
                TradeSettlementRuleCommandEntity.builder()
                        .source(tradePaySuccessEntity.getSource())
                        .channel(tradePaySuccessEntity.getChannel())
                        .userId(tradePaySuccessEntity.getUserId())
                        .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                        .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                        .build(),
                new TradeSettlementRuleFilterFactory.DynamicContext()
        );

        GroupBuyTeamEntity team = GroupBuyTeamEntity.builder()
                .teamId(back.getTeamId())
                .activityId(back.getActivityId())
                .targetCount(back.getTargetCount())
                .completeCount(back.getCompleteCount())
                .lockCount(back.getLockCount())
                .status(back.getStatus())
                .validStartTime(back.getValidStartTime())
                .validEndTime(back.getValidEndTime())
                .notifyUrl(back.getNotifyUrl())
                .build();

        boolean isNotify = repository.settlementMarketPayOrder(GroupBuyTeamSettlementAggregate.builder()
                .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
                .groupBuyTeamEntity(team)
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .build());
        if (isNotify) {
            // 仓储代理方法已经返回，通常本地事务已提交；这里不把 HTTP 包在数据库事务里。
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
}
