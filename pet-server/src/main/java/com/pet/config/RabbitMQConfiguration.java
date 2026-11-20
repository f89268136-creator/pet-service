package com.pet.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 秒杀消息队列配置：直连交换机 + 持久化队列，消息体使用 JSON。
 */
@Configuration
public class RabbitMQConfiguration {

    @Value("${pet.seckill.rabbit.exchange}")
    private String exchangeName;
    @Value("${pet.seckill.rabbit.queue}")
    private String queueName;
    @Value("${pet.seckill.rabbit.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder.bind(seckillOrderQueue()).to(seckillExchange()).with(routingKey);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}