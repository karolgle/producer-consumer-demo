package com.example.producerconsumer.interfaces;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

public interface QueueProducer<T>  extends Callable<Void> {
    Stream<T> getDataStream();
}
