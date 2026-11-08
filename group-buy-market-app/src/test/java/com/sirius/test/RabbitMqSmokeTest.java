package com.sirius.test;

import com.sirius.infrastructure.event.EventPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * RabbitMQ 基础设施集成探针：消息会进入真实 broker，不是内存替身。
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMqSmokeTest {
    @Resource
    private EventPublisher eventPublisher;


    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String routingKey;

    @Test
    public void publishFiveMessages() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            System.out.println("RabbitMQ探针消息-" + i);
            eventPublisher.publish(routingKey, "RabbitMQ探针消息-" + i);
        }
    }
}