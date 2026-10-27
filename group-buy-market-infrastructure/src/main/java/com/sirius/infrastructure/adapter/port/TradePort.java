package com.sirius.infrastructure.adapter.port;


import com.sirius.domain.trade.adapter.port.ITradePort;
import com.sirius.domain.trade.model.entity.NotifyTaskEntity;
import com.sirius.infrastructure.gateway.GroupBuyNotifyService;
import com.sirius.infrastructure.redis.IRedisService;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class TradePort implements ITradePort {
    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;

    @Override
    public String groupBuyNotify(NotifyTaskEntity task) throws Exception {
        RLock lock = redisService.getLock(task.lockKey());
        boolean acquired = false;

        try {
            // 多个实例可能扫到同一任务；没拿到锁就跳过，不记发送次数。
            // 不指定固定 leaseTime，沿用 Redisson 看门狗续期；不是“锁永不过期”。
            acquired = lock.tryLock(0, TimeUnit.SECONDS);
            if (!acquired) return "null";
            return groupBuyNotifyService.groupBuyNotify(task.getNotifyUrl(), task.getParameterJson());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "null";
        } catch (Exception e) {
            // 包括网络异常：本次未确认送达，交给通知状态机记失败尝试。
            return "error";
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
