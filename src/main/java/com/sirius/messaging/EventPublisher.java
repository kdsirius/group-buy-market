package com.sirius.messaging;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RabbitMQ 消息发布器；本节只发布字符串探针，不承载真实结算业务。
 */
@Slf4j
@Component
public class EventPublisher {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.config.producer.exchange}")
    private String exchangeName;

    /**
     * 将消息交给指定交换机和路由键；正常返回只说明发送调用完成。
     */
    public void publish(String routingKey, String message) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message, msg -> {
                // 队列和交换机持久化之外，消息本身也标记为持久化。
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return msg;
            });
            // 队列和交换机持久化之外，消息本身也标记为持久化。
        } catch (Exception e) {
            log.error("发送 RabbitMQ 消息失败 exchange:{} routingKey:{} message:{}",
                    exchangeName, routingKey, message, e);
            throw e;
        }
    }
}
