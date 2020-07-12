package com.example.producerconsumer;

import com.example.producerconsumer.interfaces.QueueConsumer;
import com.example.producerconsumer.interfaces.QueueProducer;
import com.example.producerconsumer.interfaces.TaskDataGenerator;
import com.example.producerconsumer.services.ProducersConsumersService;
import org.javatuples.Triplet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@SpringBootApplication
public class ProducerConsumerApplication implements ApplicationContextAware {

    private static int numberOfProducers;
    private static int numberOfConsumers;

    private static ApplicationContext applicationContext;

    @Value("${producers.number:2}")
    public void setNumberOfProducers(int numberOfProducers) {
        ProducerConsumerApplication.numberOfProducers = numberOfProducers;
    }

    @Value("${consumers.number:4}")
    public void setNumberOfConsumers(int numberOfConsumers) {
        ProducerConsumerApplication.numberOfConsumers = numberOfConsumers;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProducerConsumerApplication.class, args);

        ProducersConsumersService producersConsumersService = applicationContext.getBean(ProducersConsumersService.class);
        TaskDataGenerator<String> mathTaskDataGenerator = applicationContext.getBean(TaskDataGenerator.class);
        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> producersAndConsumers = producersConsumersService.prepareProducersAndConsumers(numberOfProducers, numberOfConsumers, mathTaskDataGenerator);

        producersConsumersService.run(producersAndConsumers.getValue0(), producersAndConsumers.getValue1());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
