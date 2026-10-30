package com.sirius.trigger.job;

import com.sirius.domain.trade.service.ITradeSettlementOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class GroupBuyNotifyJob {
    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Scheduled(cron = "0/15 * * * * ?")
    public void exec() {
        try {
            Map<String, Integer> map = tradeSettlementOrderService.execSettlementNotifyJob();

            // 日志优化，避免每15秒都发一条没用的日志
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                Integer value = entry.getValue();
                if (value > 0) {
                    log.info("通知任务执行结果:{}", map);
                    break;
                }
            }
        } catch (Exception e) {
            // 一轮失败要能看到原因，而不是让调度入口直接消失。
            log.error("通知任务执行失败", e);
        }
    }
}
