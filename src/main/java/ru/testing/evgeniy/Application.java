package ru.testing.evgeniy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс запуска Spring Boot приложения.
 * Включает REST и gRPC сервисы.
 * REST доступен по порту, gRPC — по отдельному порту.
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}