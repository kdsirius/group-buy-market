package com.sirius.trigger.job;

import com.sirius.domain.trade.service.ITradeSettlementOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class GroupBuyNotifyJob {
    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Scheduled(cron = "0/15 * * * * ?")
    public void exec() {
        try {
            log.info("通知任务执行结果:{}", tradeSettlementOrderService.execSettlementNotifyJob());
        } catch (Exception e) {
            // 一轮失败要能看到原因，而不是让调度入口直接消失。
            log.error("通知任务执行失败", e);
        }
    }
}
