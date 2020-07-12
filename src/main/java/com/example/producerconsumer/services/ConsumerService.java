package com.example.producerconsumer.services;

import com.example.producerconsumer.interfaces.QueueElementProcessor;
import com.udojava.evalex.Expression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ConsumerService implements QueueElementProcessor<String> {
    @Override
    public BigDecimal process(String expressionToEval) {
        //this needed to be synchronized if there were some internal stat that is globally shared but i checked the code and its threadsafe
        return new Expression(expressionToEval).eval();
    }
}
