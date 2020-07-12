package com.example.producerconsumer.interfaces;

import java.util.concurrent.Callable;

public interface QueueConsumer<T> extends Callable<Void>  {
    void consume(T operateOn);
 }
