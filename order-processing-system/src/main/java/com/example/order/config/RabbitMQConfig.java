package com.example.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration.
 */
@Configuration
public class RabbitMQConfig {


    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queues.order-paid}")
    private String orderPaidQueue;

    @Value("${rabbitmq.queues.order-failed}")
    private String orderFailedQueue;

    @Value("${rabbitmq.routing-keys.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing-keys.order-paid}")
    private String orderPaidRoutingKey;

    @Value("${rabbitmq.routing-keys.order-failed}")
    private String orderFailedRoutingKey;


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }


    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(orderCreatedQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", orderCreatedRoutingKey + ".dlq")
                .build();
    }


    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(orderPaidQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", orderPaidRoutingKey + ".dlq")
                .build();
    }


    @Bean
    public Queue orderFailedQueue() {
        return QueueBuilder.durable(orderFailedQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", orderFailedRoutingKey + ".dlq")
                .build();
    }


    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with(orderCreatedRoutingKey);
    }


    @Bean
    public Binding bindingOrderPaid(Queue orderPaidQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPaidQueue).to(exchange).with(orderPaidRoutingKey);
    }


    @Bean
    public Binding bindingOrderFailed(Queue orderFailedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderFailedQueue).to(exchange).with(orderFailedRoutingKey);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
