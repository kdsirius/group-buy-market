package com.sirius.client;

import com.sirius.client.redis.IRedisService;
import com.sirius.common.enums.NotifyResult;
import com.sirius.model.enums.NotifyType;
import com.sirius.model.vo.NotifyTaskVO;
import com.sirius.mq.publisher.RabbitMessagePublisher;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 拼团完成通知适配器；按任务快照选择 HTTP 或 MQ。
 */
@Service
public class GroupBuyNotificationClient implements IGroupBuyNotificationClient {
    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private RabbitMessagePublisher publisher;

    @Override
    public String groupBuyNotify(NotifyTaskVO task) throws Exception {
        RLock lock = redisService.getLock(task.lockKey());
        boolean acquired = false;

        try {
            acquired = lock.tryLock(0, TimeUnit.SECONDS);
            if (!acquired) {
                return NotifyResult.NULL.getCode();
            }

            if (NotifyType.HTTP.getCode().equals(task.getNotifyType())) {
                if (task.getNotifyUrl() == null || task.getNotifyUrl().trim().isEmpty()
                        || "暂无".equals(task.getNotifyUrl())) {
                    return NotifyResult.SUCCESS.getCode();
                }
                return groupBuyNotifyService.groupBuyNotify(task.getNotifyUrl(),
                        task.getParameterJson());
            }

            if (NotifyType.MQ.getCode().equals(task.getNotifyType())) {
                publisher.publish(task.getNotifyMQ(), task.getParameterJson());
                return NotifyResult.SUCCESS.getCode();
            }
            return NotifyResult.ERROR.getCode();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return NotifyResult.NULL.getCode();
        } catch (Exception e) {
            // MQ 或 HTTP 发送异常不能伪装成“没抢到锁”，否则任务不会进入重试状态。
            return NotifyResult.ERROR.getCode();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
