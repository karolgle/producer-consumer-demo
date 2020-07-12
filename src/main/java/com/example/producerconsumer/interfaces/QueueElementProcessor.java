package com.example.producerconsumer.interfaces;

import java.math.BigDecimal;

public interface QueueElementProcessor<T> {
    BigDecimal process(T operateOn);
}
