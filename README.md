## Synopsis

This is a simple project that shows an implementation of Producer-Consumer pattern. Any number of Producers and Consumers works on a shared queue.
If the queue is filled all producers stop to add new Tasks until Consumers process half of the queue elements. Producers can produce finite or infinite 
number of elements. "Poison Pills" mechanism is implemented because of the mechanis there is restriction: number of consumers must be equal or greater than number of producers. 

## Requirements
* Java 8
* Maven
* JUnit 4.12+

## Installation and run example

1. `git clone https://github.com/karolgle/producer-consumer.git`
2. `cd producer-consumer`
3. `mvn clean install spring-boot:repackage`
4. `java -jar target/producer-consumer-0.0.1-SNAPSHOT-spring-boot.jar`


## Tests
Install maven or if using IntelliJ set environment path to `{INSTALLATION_BASE}/plugins/maven/lib/maven3/bin`. 

Run following command on cloned repository:

1. Run `mvn test`

## Contributors

For now only me :).

## License

A short snippet describing the license (MIT, Apache, etc.)


Questions:
- jak gdzieś wspomniałem - niby jest spring boot i conifguracja, ale potem nie tworzonę są beany/componenty 
- niby jest lombok ale nie użyty
- nie wiem co nam tutaj dają interfejsy - rozumiem jak mamy wiele implementacji ale tutaj czasem mam wrażenie że to za dużo
- podział na warstwy a nie na domeny - co powoduje że struktura nie jest oczywista
- Generalnie - kod byłby czytelniejszy gdyby jawnie powydzielać co od czego zależy oraz zachować osobne odpowiedzialności. 
Szczerze - analizuje się ciężko.