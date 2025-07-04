package com.example.producerconsumer.services;

import com.example.producerconsumer.interfaces.QueueConsumer;
import com.example.producerconsumer.interfaces.QueueProducer;
import com.example.producerconsumer.model.PCJobContext;
import org.javatuples.Triplet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ProducersConsumersServiceTests {

    private ProducersConsumersService producersConsumersService;
    private ExecutorService producersThreadPool;
    private ExecutorService consumersThreadPool;

    @Before
    public void setUp() {
        producersConsumersService = new ProducersConsumersService(new ConsumerService(), () -> PCJobContext.<String>builder()
                .queue(new ArrayBlockingQueue<>(10))
                .counter(new AtomicInteger(0))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(10)
                .sleepTimeProducer(0)
                .sleepTimeConsumer(0)
                .build());
    }

    @After
    public void tearDown() throws InterruptedException {
        if (producersThreadPool != null) {
            producersThreadPool.shutdown();
            producersThreadPool.awaitTermination(5, TimeUnit.SECONDS);
            producersThreadPool = null;
        }
        if (consumersThreadPool != null) {
            consumersThreadPool.shutdown();
            consumersThreadPool.awaitTermination(5, TimeUnit.SECONDS);
            consumersThreadPool = null;
        }
    }

    @Test
    public void shouldConsume40Elements() throws InterruptedException {
        //given
        final int numberOfProducers = 4;
        final int numberOfConsumers = 4;
        producersThreadPool = Executors.newFixedThreadPool(numberOfProducers);
        consumersThreadPool = Executors.newFixedThreadPool(numberOfConsumers);

        ObjectFactory<PCJobContext<String>> pcJobContextObjectProvider = () -> PCJobContext.<String>builder()
                .queue(new ArrayBlockingQueue<>(10))
                .counter(new AtomicInteger(0))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(10)
                .sleepTimeProducer(10)
                .sleepTimeConsumer(20)
                .build();
        ProducersConsumersService producersConsumersService = new ProducersConsumersService(new ConsumerService(), pcJobContextObjectProvider);

        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> producersAndConsumers = producersConsumersService.prepareProducersAndConsumers(numberOfProducers, numberOfConsumers, new MathGeneratorService(5, 100, 10));

        //when invoke producers
        producersAndConsumers.getValue0()
                .parallelStream()
                .forEach(producersThreadPool::submit);
        //...and consumers
        consumersThreadPool.invokeAll(producersAndConsumers.getValue1());

        //then
        assertThat(producersAndConsumers.getValue2().size()).isGreaterThanOrEqualTo(44);
    }

    @Test
    public void shouldConsume10Elements() throws InterruptedException {
        //given
        final int numberOfProducers = 1;
        final int numberOfConsumers = 1;
        producersThreadPool = Executors.newFixedThreadPool(numberOfProducers);
        consumersThreadPool = Executors.newFixedThreadPool(numberOfConsumers);

        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> producersAndConsumers = producersConsumersService.prepareProducersAndConsumers(numberOfProducers, numberOfConsumers, new MathGeneratorService(5, 100, 10));

        //when invoke producers
        producersAndConsumers.getValue0()
                .parallelStream()
                .forEach(producersThreadPool::submit);
        //...and consumers
        consumersThreadPool.invokeAll(producersAndConsumers.getValue1());

        //then
        assertThat(producersAndConsumers.getValue2().size()).isEqualTo(11);
    }


    @Test
    public void shouldConsume20ElementsWithSomeOfBlocking() throws InterruptedException {
        //given
        final int numberOfProducers = 2;
        final int numberOfConsumers = 5;
        producersThreadPool = Executors.newFixedThreadPool(numberOfProducers);
        consumersThreadPool = Executors.newFixedThreadPool(numberOfConsumers);

        ObjectFactory<PCJobContext<String>> pcJobContextObjectProvider = () -> PCJobContext.<String>builder()
                .queue(new ArrayBlockingQueue<>(10))
                .counter(new AtomicInteger(0))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(10)
                .sleepTimeProducer(10)
                .sleepTimeConsumer(100)
                .build();
        ProducersConsumersService producersConsumersService = new ProducersConsumersService(new ConsumerService(), pcJobContextObjectProvider);

        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> producersAndConsumers = producersConsumersService.prepareProducersAndConsumers(numberOfProducers, numberOfConsumers, new MathGeneratorService(5, 100, 10));
        //when invoke producers
        producersAndConsumers.getValue0()
                .forEach(producersThreadPool::submit);
        //...and consumers
        consumersThreadPool.invokeAll(producersAndConsumers.getValue1());

        //then
        assertThat(producersAndConsumers.getValue2().size()).isGreaterThanOrEqualTo(25);
    }

    @Test
    public void shouldConsume500Elements() throws InterruptedException {
        //given
        final int numberOfProducers = 5;
        final int numberOfConsumers = 5;
        producersThreadPool = Executors.newFixedThreadPool(numberOfProducers);
        consumersThreadPool = Executors.newFixedThreadPool(numberOfConsumers);

        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> producersAndConsumers = producersConsumersService.prepareProducersAndConsumers(numberOfProducers, numberOfConsumers, new MathGeneratorService(250, 500, 100));
        //when invoke producers
        producersAndConsumers.getValue0()
                .forEach(producersThreadPool::submit);
        //...and consumers
        consumersThreadPool.invokeAll(producersAndConsumers.getValue1());

        //then
        assertThat(producersAndConsumers.getValue2().size()).isGreaterThanOrEqualTo(505);
    }

    @Test()
    public void shouldConsume14ElementsWithSomeOfBlocking() throws InterruptedException {
        //given
        final int numberOfProducers = 7;
        final int numberOfConsumers = 8;
        producersThreadPool = Executors.newFixedThreadPool(numberOfProducers);
        consumersThreadPool = Executors.newFixedThreadPool(numberOfConsumers);

        ObjectFactory<PCJobContext<String>> pcJobContextObjectProvider = () -> PCJobContext.<String>builder()
                .queue(new ArrayBlockingQueue<>(10))
                .counter(new AtomicInteger(0))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(10)
                .sleepTimeProducer(100)
                .sleepTimeConsumer(10)
                .build();
        ProducersConsumersService producersConsumersService = new ProducersConsumersService(new ConsumerService(), pcJobContextObjectProvider);


        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> producersAndConsumers = producersConsumersService.prepareProducersAndConsumers(numberOfProducers, numberOfConsumers, new MathGeneratorService(1, 1, 1));
        //when invoke producers
        producersAndConsumers.getValue0()
                .forEach(producersThreadPool::submit);
        //...and consumers
        consumersThreadPool.invokeAll(producersAndConsumers.getValue1());

        //then
        assertThat(producersAndConsumers.getValue2().size()).isGreaterThanOrEqualTo(14);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNumberOfProducersIs0() {
        //when
        producersConsumersService.prepareProducersAndConsumers(0, 4, new MathGeneratorService(1, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNumberOfConsumersIs0() {
        //when
        producersConsumersService.prepareProducersAndConsumers(1, 0, new MathGeneratorService(1, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNumberOfProducersIsGreaterThenConsumers() {
        //when
        producersConsumersService.prepareProducersAndConsumers(6, 5, new MathGeneratorService(1, 1, 1));
    }

}
