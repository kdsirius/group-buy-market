package com.sirius.infrastructure.adapter.port;


import com.sirius.domain.trade.adapter.port.ITradePort;
import com.sirius.domain.trade.model.entity.NotifyTaskEntity;
import com.sirius.domain.trade.model.valobj.NotifyTypeEnumVO;
import com.sirius.infrastructure.event.EventPublisher;
import com.sirius.infrastructure.gateway.GroupBuyNotifyService;
import com.sirius.infrastructure.redis.IRedisService;
import com.sirius.types.enums.NotifyTaskHTTPEnumVO;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 拼团完成通知适配器；按任务快照选择 HTTP 或 MQ。
 */
@Service
public class TradePort implements ITradePort {
    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;

    @Override
    public String groupBuyNotify(NotifyTaskEntity task) throws Exception {
        RLock lock = redisService.getLock(task.lockKey());
        boolean acquired = false;

        try {
            acquired = lock.tryLock(0, TimeUnit.SECONDS);
            if (!acquired) {
                return NotifyTaskHTTPEnumVO.NULL.getCode();
            }

            if (NotifyTypeEnumVO.HTTP.getCode().equals(task.getNotifyType())) {
                if (task.getNotifyUrl() == null || task.getNotifyUrl().trim().isEmpty()
                        || "暂无".equals(task.getNotifyUrl())) {
                    return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                }
                return groupBuyNotifyService.groupBuyNotify(task.getNotifyUrl(),
                        task.getParameterJson());
            }

            if (NotifyTypeEnumVO.MQ.getCode().equals(task.getNotifyType())) {
                publisher.publish(task.getNotifyMQ(), task.getParameterJson());
                return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
            }
            return NotifyTaskHTTPEnumVO.ERROR.getCode();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        } catch (Exception e) {
            // MQ 或 HTTP 发送异常不能伪装成“没抢到锁”，否则任务不会进入重试状态。
            return NotifyTaskHTTPEnumVO.ERROR.getCode();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
