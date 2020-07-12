package com.example.producerconsumer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class PCJobContext<T> {
    // shared BlockingQueue used by all Producers and Consumers
    @NonNull
    final private BlockingQueue<T> queue;
    // counter used to monitor number of elements in queue, based on this variable waitForEmptyingHalfOfTheQueue is updated
    @NonNull
    final private AtomicInteger counter;
    // flag used to stop temporally all Producers until queue is half emptied
    @NonNull
    final private AtomicBoolean waitForEmptyingHalfOfTheQueue;
    @NonNull
    final private int queueCapacity;
    @NonNull
    final private int sleepTimeConsumer;
    @NonNull
    final private int sleepTimeProducer;

    @Builder
    public PCJobContext(@NonNull BlockingQueue<T> queue, @NonNull AtomicInteger counter, @NonNull AtomicBoolean waitForEmptyingHalfOfTheQueue, @NonNull int queueCapacity, @NonNull int sleepTimeConsumer, @NonNull int sleepTimeProducer) {
        if (queueCapacity < 1) {
            throw new IllegalArgumentException("Shared queue queueCapacity must be greater then 0.");
        }

        if (sleepTimeConsumer < 0 || sleepTimeProducer < 0) {
            throw new IllegalArgumentException("Sleep time must be 0 or greater.");
        }
        this.queue = queue;
        this.counter = counter;
        this.waitForEmptyingHalfOfTheQueue = waitForEmptyingHalfOfTheQueue;
        this.queueCapacity = queueCapacity;
        this.sleepTimeConsumer = sleepTimeConsumer;
        this.sleepTimeProducer = sleepTimeProducer;
    }
}
