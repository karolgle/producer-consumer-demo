package com.example.producerconsumer;

import com.example.producerconsumer.model.PCJobContext;
import com.example.producerconsumer.services.ConsumerService;
import com.example.producerconsumer.services.MathGeneratorService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.producerconsumer.QueueVisualizer;

import static org.assertj.core.api.Assertions.assertThat;
public class QueueConsumerImplTest {

    final ConsumerService  consumerService = new ConsumerService();

    QueueVisualizer queueVisualizer;

    PCJobContext<String> pcJobContext;
    @Before
    public void setUp() {
        pcJobContext = PCJobContext.<String>builder()
                .queue( new ArrayBlockingQueue<>(1))
                .counter(new AtomicInteger(0))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(1)
                .sleepTimeConsumer(0)
                .sleepTimeProducer(0)
                .build();
        queueVisualizer = new QueueVisualizer(1);
    }

    @Test
    public void shouldConsumePoisonPill() {
        //given
        List<String> messagesOutputList = new ArrayList<>();
        //when
        new QueueConsumerImpl("C1", messagesOutputList, consumerService, pcJobContext, queueVisualizer).consume(MathGeneratorService.POISON_PILL);
        //then
        assertThat(messagesOutputList).containsOnly("Poison pill swallowed by: C1");
    }

    @Test
    public void shouldConsumeElement() {
        //given
        List<String> messagesOutputList = new ArrayList<>();
        //when
        new QueueConsumerImpl("C1", messagesOutputList, consumerService, pcJobContext, queueVisualizer).consume("2+2");
        //then
        assertThat(messagesOutputList).containsOnly("Consumer C1: 2+2 = 4");
    }

    @Test
    public void shouldStartBlockingQueue() {
        //given
        List<String> messagesOutputList = new ArrayList<>();
        //when
        new QueueConsumerImpl("C1", messagesOutputList, consumerService, PCJobContext.<String>builder()
                .queue( new ArrayBlockingQueue<>(2))
                .counter(new AtomicInteger(2))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(false))
                .queueCapacity(2)
                .sleepTimeConsumer(0)
                .sleepTimeProducer(0)
                .build(), new QueueVisualizer(2)).consume("2+2");
        //then
        assertThat(messagesOutputList).containsOnly("Consumer C1: 2+2 = 4","Start blocking Producers");
    }

    @Test
    public void shouldStopBlockingQueue() {
        //given
        List<String> messagesOutputList = new ArrayList<>();
        //when
        new QueueConsumerImpl("C1", messagesOutputList, consumerService, PCJobContext.<String>builder()
                .queue( new ArrayBlockingQueue<>(2))
                .counter(new AtomicInteger(1))
                .waitForEmptyingHalfOfTheQueue(new AtomicBoolean(true))
                .queueCapacity(2)
                .sleepTimeConsumer(0)
                .sleepTimeProducer(0)
                .build(), new QueueVisualizer(2)).consume("2+2");
        //then
        assertThat(messagesOutputList).containsOnly("Consumer C1: 2+2 = 4", "Stop blocking Producers");
    }

    @Test(expected = ArithmeticException.class)
    public void throwsExceptionOnConsumeElement() {
        //given
        List<String> messagesOutputList = new ArrayList<>();
        //when
        new QueueConsumerImpl("C1", messagesOutputList, consumerService, pcJobContext, queueVisualizer).consume("2/0");
    }
}
