package com.example.producerconsumer.config;

import com.example.producerconsumer.interfaces.TaskDataGenerator;
import com.example.producerconsumer.services.MathGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class TaskDataGeneratorConfig {

    private int generatorExprMinLength;
    private int generatorExprMaxLength;

    @Value("${generator.expr.min:5}")
    public void setGeneratorExprMinLength(int generatorExprMinLength) {
        this.generatorExprMinLength = generatorExprMinLength;
    }

    @Value("${generator.expr.max:100}")
    public void setGeneratorExprMaxLength(int generatorExprMaxLength) {
        this.generatorExprMaxLength = generatorExprMaxLength;
    }

    @Bean()
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TaskDataGenerator<String> getTaskDataGenerator() {
        return new MathGeneratorService(generatorExprMinLength, generatorExprMaxLength);
    }
}
