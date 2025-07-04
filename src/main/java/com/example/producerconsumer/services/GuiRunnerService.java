package com.example.producerconsumer.services;

import com.example.producerconsumer.interfaces.QueueConsumer;
import com.example.producerconsumer.interfaces.QueueProducer;
import com.example.producerconsumer.interfaces.TaskDataGenerator;
import org.javatuples.Triplet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class GuiRunnerService {

    private final ProducersConsumersService producersConsumersService;
    private final TaskDataGenerator<String> taskDataGenerator;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> current;

    public GuiRunnerService(ProducersConsumersService producersConsumersService, TaskDataGenerator<String> taskDataGenerator) {
        this.producersConsumersService = producersConsumersService;
        this.taskDataGenerator = taskDataGenerator;
    }

    public synchronized void start(int producers, int consumers) {
        if (current != null && !current.isDone()) {
            throw new IllegalStateException("Already running");
        }
        Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> data =
                producersConsumersService.prepareProducersAndConsumers(producers, consumers, taskDataGenerator);
        current = executorService.submit(() -> producersConsumersService.run(data.getValue0(), data.getValue1()));
    }

    public synchronized void stop() {
        if (current != null && !current.isDone()) {
            producersConsumersService.stop();
            current.cancel(true);
        }
    }

    public synchronized boolean isRunning() {
        return current != null && !current.isDone();
    }
}
