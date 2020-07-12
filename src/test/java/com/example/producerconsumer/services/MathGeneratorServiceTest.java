package com.example.producerconsumer.services;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MathGeneratorServiceTest {

    public static final int GENERATOR_EXPR_MIN_LENGTH = 5;
    public static final int GENERATOR_EXPR_MAX_LENGTH = 100;
    private MathGeneratorService mg;

    @Before
    public void setUp() {
        mg = new MathGeneratorService(GENERATOR_EXPR_MIN_LENGTH, GENERATOR_EXPR_MAX_LENGTH);
    }

    @Test()
    public void shouldGenerateRandomString() {
        //then
        mg.generator().limit(100).forEach(s -> assertThat(s).matches("([1-9]+[0-9]*[+\\-*/])*[1-9]+[0-9]*"));
    }

    @Test()
    public void shouldExprBeBetween5and100() {
        //then
        mg.generator().limit(100).forEach(s -> assertThat(s.length()).isGreaterThanOrEqualTo(GENERATOR_EXPR_MIN_LENGTH).isLessThanOrEqualTo(GENERATOR_EXPR_MAX_LENGTH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenNegativMin() {
        //then
        //noinspection ResultOfMethodCallIgnored
        new MathGeneratorService(-1, 100).generator().limit(1).findFirst();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenMixMinMax() {
        //then
        //noinspection ResultOfMethodCallIgnored
        new MathGeneratorService(100, 5).generator().limit(1).findFirst();
    }

    @Test()
    public void shouldReturnStreamWithOnePoisonPill() {

        //when
        List<String> listOfExpr = mg.addPoisonPill(mg.generator().limit(10), 1).collect(Collectors.toList());

        //then
        assertThat(listOfExpr.size()).isEqualTo(11);
        assertThat(listOfExpr).containsOnlyOnce(MathGeneratorService.POISON_PILL);
        assertThat(listOfExpr.get(10)).isEqualTo(MathGeneratorService.POISON_PILL);
    }

    @Test()
    public void shouldReturnStreamWith5PoisonPills() {

        //when
        List<String> listOfExpr = mg.addPoisonPill(mg.generator().limit(10), GENERATOR_EXPR_MIN_LENGTH).skip(10).collect(Collectors.toList());

        //then
        assertThat(listOfExpr.size()).isEqualTo(GENERATOR_EXPR_MIN_LENGTH);
        assertThat(listOfExpr).containsOnly(MathGeneratorService.POISON_PILL);
    }

    @Test()
    public void shouldReturnStreamWith0PoisonPills() {

        //when
        List<String> listOfExpr = mg.addPoisonPill(mg.generator().limit(10), 0).collect(Collectors.toList());

        //then
        assertThat(listOfExpr.size()).isEqualTo(10);
        assertThat(listOfExpr).doesNotContain(MathGeneratorService.POISON_PILL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNegativeNumberOfPoisonPills() {

        //then
        //noinspection ResultOfMethodCallIgnored
        mg.addPoisonPill(mg.generator().limit(10), -1).collect(Collectors.toList());
    }
}
