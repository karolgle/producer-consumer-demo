package com.example.producerconsumer.config;

import com.example.producerconsumer.model.PCJobContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class PCJobContextConfig {

    private int sleepTimeProducers;
    private int sleepTimeConsumers;
    private int queueCapacity;

    @Value("${producers.sleeptime:0}")
    public void setSleepTimeProducers(int sleepTimeProducers) {
        this.sleepTimeProducers = sleepTimeProducers;
    }

    @Value("${consumers.sleeptime:0}")
    public void setSleepTimeConsumers(int sleepTimeConsumers) {
        this.sleepTimeConsumers = sleepTimeConsumers;
    }

    @Value("${queue.max.capacity:10}")
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    @Bean()
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PCJobContext<String> getPCJobContext() {
        return PCJobContext.<String>builder()
                .queue(new ArrayBlockingQueue<>(queueCapacity))
                .counter(new AtomicInteger(0))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(queueCapacity)
                .sleepTimeProducer(sleepTimeProducers)
                .sleepTimeConsumer(sleepTimeConsumers)
                .build();
    }
}
