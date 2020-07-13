package com.example.producerconsumer;

import com.example.producerconsumer.interfaces.QueueConsumer;
import com.example.producerconsumer.interfaces.QueueElementProcessor;
import com.example.producerconsumer.model.PCJobContext;
import com.example.producerconsumer.services.MathGeneratorService;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class QueueConsumerImpl implements QueueConsumer<String> {

    final private List<String> messagesOutputList;

    final private QueueElementProcessor<String> consumerService;
    final private PCJobContext<String> pcJobContext;
    final private String name;

    public QueueConsumerImpl(String name, List<String> messagesOutputList, QueueElementProcessor<String> consumerService, PCJobContext<String> pcJobContext) {
        this.name = name;
        this.messagesOutputList = messagesOutputList;
        this.consumerService = consumerService;
        this.pcJobContext = pcJobContext;
    }
    
    @Override
    //kolejność metod powinna być odwrotna
    public void consume(String operateOn) {
        //ten kawalek kodu powinien być wydzielony - SRP

        String message;
        if (operateOn.equals(MathGeneratorService.POISON_PILL)) {
            message = String.format("Poison pill swallowed by: %s", name);
            messagesOutputList.add(message);

            //logger ?
            System.out.println(message);
            Thread.currentThread()
                    .interrupt();
            return;
        }

        // this needs to be synchronized on shared object for all TaskConsumer and TaskProducers.
        // its not optimal as thread are blocked but if Producers need to resume work exactly when half of the queue is empty this is the solution
        // if exact resuming would not be an issue we could take of the synchronized blocked(or change it to synchronized(this))
        // and consumer.consume would still work correctly but producers could start little earlier and the prints to console could be inconsistent
        // ...and if there were no 'half of the queue is empty' condition at all then using Spliterator and Stream's would be much cleaner approach.
        synchronized (pcJobContext.getWaitForEmptyingHalfOfTheQueue()) {
            //dlaczego czekamy aż połoa kolejki będzie pusta? Jaki to będzie miało wpływ gdy kolejka będzie miała np 10_000 elementów - czekamy aż 5_000 będzie skonsumowane?

            // operation that will be done
            message = String.format("Consumer %s: %s = %s", name, operateOn, consumerService.process(operateOn));
            messagesOutputList.add(message);
            //logger

            System.out.println(message);

            // block Producers if queue is full
            if (!pcJobContext.getWaitForEmptyingHalfOfTheQueue().get() && pcJobContext.getCounter().get() >= pcJobContext.getQueueCapacity()) {
                pcJobContext.getWaitForEmptyingHalfOfTheQueue().set(true);
                message = "Start blocking Producers";
                messagesOutputList.add(message);
                System.out.println(message);

                //kod który wykonuje logikę z kodem który buduje wiadomości i loguje jest pomieszany co wpływa na nieczytelność
            }
            // decrement only if > 1
            int checkIfProducersCanStartAddingToQueue = pcJobContext.getCounter().updateAndGet(i -> i > 0 ? i - 1 : i);

            // stop blocking Producers if queue is half empty
            if (pcJobContext.getWaitForEmptyingHalfOfTheQueue().get() && checkIfProducersCanStartAddingToQueue < (pcJobContext.getQueueCapacity() / 2)) {
                pcJobContext.getWaitForEmptyingHalfOfTheQueue().set(false);
                message = "Stop blocking Producers";
                messagesOutputList.add(message);
                System.out.println(message);
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public Void call() throws Exception {
        while (true) {
            consume(pcJobContext.getQueue().take());
            // simulate a long running process
            MILLISECONDS.sleep(pcJobContext.getSleepTimeConsumer());
        }
    }
}
