package com.example.sensorspring.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "app.events";
    public static final String RK_FILE_UPLOADED = "file.uploaded";
    public static final String RK_USER_REGISTERED = "user.registered";
    public static final String Q_FILE_UPLOADED = "file.uploaded.q";
    public static final String Q_USER_REGISTERED = "user.registered.q";

    @Bean public TopicExchange appExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean public Queue fileUploadedQueue() { return QueueBuilder.durable(Q_FILE_UPLOADED).build(); }
    @Bean public Queue userRegisteredQueue() { return QueueBuilder.durable(Q_USER_REGISTERED).build(); }
    @Bean public Binding bindFileUploaded() { return BindingBuilder.bind(fileUploadedQueue()).to(appExchange()).with(RK_FILE_UPLOADED); }
    @Bean public Binding bindUserRegistered() { return BindingBuilder.bind(userRegisteredQueue()).to(appExchange()).with(RK_USER_REGISTERED); }
    @Bean public Jackson2JsonMessageConverter jackson2JsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter c) { RabbitTemplate t = new RabbitTemplate(cf); t.setMessageConverter(c); return t; }
}
