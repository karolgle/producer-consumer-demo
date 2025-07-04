package com.example.producerconsumer;

import com.example.producerconsumer.interfaces.QueueProducer;
import com.example.producerconsumer.model.PCJobContext;
import com.example.producerconsumer.services.MathGeneratorService;
import com.example.producerconsumer.QueueVisualizer;

import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class QueueProducerImpl implements QueueProducer<String> {

    final private PCJobContext<String> pcJobContext;
    final private Stream<String> exprStream;
    final private String name;
    final private QueueVisualizer queueVisualizer;

    public QueueProducerImpl(String name, PCJobContext<String> pcJobContext, Stream<String> exprStream, QueueVisualizer queueVisualizer) {
        this.name = name;
        this.pcJobContext = pcJobContext;
        this.exprStream = exprStream;
        this.queueVisualizer = queueVisualizer;
    }

    @Override
    public Void call() {
        exprStream.forEach(s -> {
            // Use a do-while loop because queue.offer is non-blocking; when the
            // queue is full offer returns false. Repeating until it succeeds
            // prevents losing elements while the forEach loop continues.
            do {
                try {
                    // simulate a long running process
                    MILLISECONDS.sleep(pcJobContext.getSleepTimeProducer());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread()
                            .interrupt();
                }
            } while (!addToQueue(s));
        });

        return null;
    }

    private boolean addToQueue(String stringExpression) {
        boolean isAddedToQueue = false;
        synchronized (pcJobContext.getWaitForEmptyingHalfOfTheQueue()) {
            if (pcJobContext.getCounter().get() < pcJobContext.getQueueCapacity() && !pcJobContext.getWaitForEmptyingHalfOfTheQueue().get()) {
                isAddedToQueue = pcJobContext.getQueue().offer(stringExpression);

                System.out.println("Producer " + name + ": " + (isAddedToQueue ? "adds " + stringExpression + " to queue" : "DIDN'T manage to add"));

                if (isAddedToQueue) {
                    int newSize = pcJobContext.getCounter().incrementAndGet();
                    queueVisualizer.update(newSize);
                }
            } else if (stringExpression.equals(MathGeneratorService.POISON_PILL)) {
                isAddedToQueue = pcJobContext.getQueue().offer(stringExpression);
            }
        }
        return isAddedToQueue;
    }

    @Override
    public Stream<String> getDataStream() {
        return exprStream;
    }

}
