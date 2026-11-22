package com.sirius.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 拼团侧 RabbitMQ 探针消费者；本节只打印消息，不修改订单或通知状态。
 */
@Slf4j
@Component
public class TeamSuccessMessageListener {

    @RabbitListener(
            ackMode = "AUTO",
            bindings = @QueueBinding(
                    value = @Queue(
                            value = "${spring.rabbitmq.config.producer.topic_team_success.queue}",
                            durable = "true"),
                    exchange = @Exchange(
                            value = "${spring.rabbitmq.config.producer.exchange}",
                            type = ExchangeTypes.TOPIC,
                            durable = "true"),
                    key = "${spring.rabbitmq.config.producer.topic_team_success.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("拼团侧收到 RabbitMQ 消息:{}", message);
    }
}
