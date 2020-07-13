package com.example.producerconsumer.services;

import com.example.producerconsumer.QueueConsumerImpl;
import com.example.producerconsumer.QueueProducerImpl;
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

/**
 * Class that creates List of Producers and List of Consumers that that are linked with each other by the same BlockingQueue.
 */
@Service
//nazwa service nosi znamiona czegość ogólnego - warstwy. Wg mnie lepsze podejście to organizacja kodu w obszary/domeny/funkcjonalności.
// Gdzie jawnie wskazujemy zakres odpowiedzialności. Natomiast tutaj mamy: przygotowanie, walidacje i uruchomienie w związku z tym nie zachowujemy SRP
public class ProducersConsumersService {


    private final ConsumerService consumerService;
    private final PCJobContext<String> pcJobContext;

    //skoro jest lombok w dependencies to mozna tutaj użyć @RequiredArgsConstructor
    //dlaczego producer zalezy od consumera?
    @Autowired
    public ProducersConsumersService(ConsumerService consumerService, ObjectFactory<PCJobContext<String>> pcJobContextObjectProvider) {
        this.consumerService = consumerService;
        this.pcJobContext = pcJobContextObjectProvider.getObject();
    }

    /**
     * @param numberOfProducers - number of working producers
     * @param numberOfConsumers - number of working consumers
     * @return -  The list of producers and consumers
     */

    //final w parametrach oraz zmiennych jest zbędny
    //comentarze w kodzie wg. mnie często traca na ważności - https://twitter.com/unclebobmartin/status/870311898545258497
    //użycie tutaj Triplet zaciemnia obraz - skąd wiadomo że trzecim obiektem jest messageList - gdyby to było ukryte za API i byłaby to 3rd party library to nie wiadomo jak to interpretowac - chyba że jest dokumentacja ale jak wspomniałem wyżej
    // jest ona często nieaktualne, poza tym - już w samej próbie udokumentowania poprzez @return, nie wiadomo co to jest ten trzeci parametr
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
            taskProducerImpls.add(new QueueProducerImpl("P" + i, pcJobContext, mathGeneratorService.addPoisonPill(mathGeneratorService.generator(), isLastProducer ? pillsPerProducer + pillsToBeAddedToLastProducer : pillsPerProducer)));
        }

        List<QueueConsumer<String>> taskConsumerImpls = new ArrayList<>();
        for (int i = 0; i < numberOfConsumers; i++) {
            taskConsumerImpls.add(new QueueConsumerImpl("C" + i, messageList, this.consumerService, pcJobContext));
        }

        return new Triplet<>(taskProducerImpls, taskConsumerImpls, messageList);
    }

    private void validateParameters(int numberOfProducers, int numberOfConsumers) {
        if (numberOfConsumers < 1 || numberOfProducers < 1) {
            throw new IllegalArgumentException("Number of producers and consumers must be greater then 0.");
        }
        // because we implemented POISON_PILL(stopping of all consumers when producers finish theirs work)
        // there is a case when if numberOfProducers > numberOfConsumers the consumers stop consuming the tasks,
        // see how pillsPerProducer and pillsToBeAddedToLastProducer are used
        if (numberOfConsumers < numberOfProducers) {
            throw new IllegalArgumentException("Number of consumers must be greater or equal then producers.");
        }
    }

    public void run(List<QueueProducer<String>> producers, List<QueueConsumer<String>> consumers) {
        //czy tutaj nie powinniśmy rozdzielić tworzenia pul wątków i inicjacji executorów od samego wrzucenia consumerów/producentów.
        // rozmiar pól nie jest podany excplicite tylko wynika z rozmiaru przekazanych producentów i koonsumetów.
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
        }
    }
}
