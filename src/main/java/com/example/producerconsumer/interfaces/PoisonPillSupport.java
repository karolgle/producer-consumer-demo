package com.example.producerconsumer.interfaces;

import java.util.stream.Stream;

public interface PoisonPillSupport<T> {
    Stream<T> addPoisonPill(Stream<T> input, int numberOfPoisonPills);
}
