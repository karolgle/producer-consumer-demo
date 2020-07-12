package com.example.producerconsumer.interfaces;

import java.util.stream.Stream;

public interface TaskDataGenerator<T> {
    /*Return generator that prepare data for Producer*/
    Stream<T> generator();
    Stream<T> addPoisonPill(Stream<T> input, int numberOfPoisonPills);
}
