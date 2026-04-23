package com.finance.tracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, YearMonth.class, source -> {
            try {
                return YearMonth.parse(source);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Неверный формат месяца: '" + source + "'. Ожидается yyyy-MM");
            }
        });
    }
}
