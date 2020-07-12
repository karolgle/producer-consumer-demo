package com.example.producerconsumer.services;

import com.example.producerconsumer.interfaces.TaskDataGenerator;
import com.mifmif.common.regex.Generex;

import java.util.stream.Stream;
public class MathGeneratorService implements TaskDataGenerator<String> {

    public static final String POISON_PILL = "POISON_PILL";

    private final int generatorExprMinLength;
    private final int generatorExprMaxLength;
    private final int limit;

    public MathGeneratorService(int generatorExprMinLength, int generatorExprMaxLength) {
        if(generatorExprMinLength<0 || generatorExprMinLength>generatorExprMaxLength){
            throw new IllegalArgumentException();
        }
        this.generatorExprMinLength = generatorExprMinLength;
        this.generatorExprMaxLength = generatorExprMaxLength;
        //infinite stream
        this.limit = -1;
    }

    public MathGeneratorService(int generatorExprMinLength, int generatorExprMaxLength, int limit) {
        if(generatorExprMinLength<0 || generatorExprMinLength>generatorExprMaxLength || limit<1){
            throw new IllegalArgumentException();
        }
        this.generatorExprMinLength = generatorExprMinLength;
        this.generatorExprMaxLength = generatorExprMaxLength;
        this.limit = limit;
    }

    @Override
    public Stream<String> generator() {
        Generex generex = new Generex("([1-9]+[0-9]*[+\\-*/])*[1-9]+[0-9]*");
        Stream<String> stringStream =  Stream.generate(() -> generex.random(generatorExprMinLength, generatorExprMaxLength));
        return limit < 1 ? stringStream : stringStream.limit(limit);
    }

    @Override
    public Stream<String> addPoisonPill(Stream<String> input, int numberOfPoisonPills) {
        return Stream.concat(input, Stream.generate(() -> POISON_PILL).limit(numberOfPoisonPills));
    }

}
