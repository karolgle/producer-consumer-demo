package com.example.producerconsumer.services;

import com.example.producerconsumer.QueueConsumerImpl;
import com.example.producerconsumer.QueueProducerImpl;
import com.example.producerconsumer.QueueVisualizer;
import com.example.producerconsumer.interfaces.QueueConsumer;
import com.example.producerconsumer.interfaces.QueueProducer;
import com.example.producerconsumer.interfaces.TaskDataGenerator;
import com.example.producerconsumer.model.PCJobContext;
import org.javatuples.Triplet;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class that creates List of Producers and List of Consumers that that are linked with each other by the same BlockingQueue.
 */
@Service
public class ProducersConsumersService {


    private final ConsumerService consumerService;
    private final PCJobContext<String> pcJobContext;
    private final QueueVisualizer queueVisualizer;

    @Autowired
    public ProducersConsumersService(ConsumerService consumerService, ObjectFactory<PCJobContext<String>> pcJobContextObjectProvider, QueueVisualizer queueVisualizer) {
        this.consumerService = consumerService;
        this.pcJobContext = pcJobContextObjectProvider.getObject();
        this.queueVisualizer = queueVisualizer;
    }

    /**
     * @param numberOfProducers - number of working producers
     * @param numberOfConsumers - number of working consumers
     * @return -  The list of producers and consumers
     */
    public Triplet<List<QueueProducer<String>>, List<QueueConsumer<String>>, List<String>> prepareProducersAndConsumers( final int numberOfProducers, final int numberOfConsumers, final TaskDataGenerator<String> mathGeneratorService) {

        //the list is used to register messages(strings) that are output to console, it's useful for further processing and testing
        final List<String> messageList = Collections.synchronizedList(new ArrayList<>());

        validateParameters(numberOfProducers, numberOfConsumers);

        // in case when there is more consumers then producers some of the producers will need to produce more then one POISON_PILL
        final int pillsPerProducer = numberOfConsumers / numberOfProducers;

        // leftovers that need to be added to any of the producers for number of POISON_PILLs to be equal the number of consumers
        // if there were no check if (numberOfConsumers >= numberOfProducers) this leftovers should be spread across all producers
        // which would lower the chance of corner case where all consumers stopped working before the producers
        final int pillsToBeAddedToLastProducer = numberOfConsumers % numberOfProducers;

        List<QueueProducer<String>> taskProducerImpls = new ArrayList<>();
        for (int i = 0; i < numberOfProducers; i++) {
            boolean isLastProducer = i == numberOfProducers - 1;
            taskProducerImpls.add(new QueueProducerImpl("P" + i, pcJobContext,
                    mathGeneratorService.addPoisonPill(mathGeneratorService.generator(),
                            isLastProducer ? pillsPerProducer + pillsToBeAddedToLastProducer : pillsPerProducer),
                    queueVisualizer));
        }

        List<QueueConsumer<String>> taskConsumerImpls = new ArrayList<>();
        for (int i = 0; i < numberOfConsumers; i++) {
            taskConsumerImpls.add(new QueueConsumerImpl("C" + i, messageList, this.consumerService, pcJobContext, queueVisualizer));
        }

        return new Triplet<>(taskProducerImpls, taskConsumerImpls, messageList);
    }

    private void validateParameters(int numberOfProducers, int numberOfConsumers) {
        if (numberOfConsumers < 1 || numberOfProducers < 1) {
            throw new IllegalArgumentException("Number of producers and consumers must be greater than 0.");
        }
        // because we implemented POISON_PILL(stopping of all consumers when producers finish theirs work)
        // there is a case when if numberOfProducers > numberOfConsumers the consumers stop consuming the tasks,
        // see how pillsPerProducer and pillsToBeAddedToLastProducer are used
        if (numberOfConsumers < numberOfProducers) {
            throw new IllegalArgumentException("Number of consumers must be greater or equal than producers.");
        }
    }

    public void run(List<QueueProducer<String>> producers, List<QueueConsumer<String>> consumers) {
        final ExecutorService producersThreadPool = Executors.newFixedThreadPool(producers.size());
        final ExecutorService consumersThreadPool = Executors.newFixedThreadPool(consumers.size());

        // run producers - submit() method DOES NOT wait for the completion of all task
        producers.forEach(producersThreadPool::submit);

        try {
            @SuppressWarnings("unchecked")
            List<Callable<Void>> callable = (List<Callable<Void>>) (List<?>) consumers;
            //...and consumers - invokeAll method DOES wait for the completion of all task
            consumersThreadPool.invokeAll(callable);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            producersThreadPool.shutdown();
            consumersThreadPool.shutdown();
            try {
                producersThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                consumersThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                queueVisualizer.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
