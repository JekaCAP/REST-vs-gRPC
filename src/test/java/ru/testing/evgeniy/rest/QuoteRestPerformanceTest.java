package ru.testing.evgeniy.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Автономный нагрузочный тест REST сервиса цитат с использованием {@link MockMvc}.
 *
 * <p>Тест демонстрирует:
 * <ul>
 *     <li>Использование {@link WebMvcTest} для поднятия только MVC слоя без всего приложения.</li>
 *     <li>Отправку большого количества POST-запросов к контроллеру {@link QuoteRestController}.</li>
 *     <li>Измерение времени обработки всех запросов и вычисление средней задержки на один запрос.</li>
 * </ul>
 *
 * <p>Константа {@link #NUM_REQUESTS} задает количество POST-запросов для теста.
 *
 * <p>Особенности:
 * <ul>
 *     <li>{@link MockMvc} выполняет запросы синхронно, поэтому параллельное выполнение через {@link IntStream#parallel()}
 *     ускоряет тестирование, но каждое выполнение метода perform() блокирует поток до получения ответа.</li>
 *     <li>В случае реального REST сервера с асинхронной обработкой можно использовать {@code WebClient}
 *     и {@code Flux/Mono} для асинхронного тестирования.</li>
 * </ul>
 *
 * <p>Для изменения парадигмы выполнения:
 * <ul>
 *     <li>Оставить синхронное выполнение (как сейчас) – ничего менять не нужно.</li>
 *     <li>Для "чисто последовательного" теста без параллельности убрать {@code parallel()}:</li>
 *     <pre>
 *     IntStream.range(0, NUM_REQUESTS).forEach(i -> {
 *         mockMvc.perform(post("/quotes")
 *                 .content("Quote " + i)
 *                 .contentType(MediaType.TEXT_PLAIN))
 *                 .andExpect(status().isOk());
 *     });
 *     </pre>
 * </ul>
 */
@WebMvcTest
@DisplayName("Автономный REST нагрузочный тест (@WebMvcTest)")
public class QuoteRestPerformanceTest {

    /**
     * Количество POST-запросов для теста
     */
    private static final int NUM_REQUESTS = 10000;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Нагрузочный тест: отправляет {@link #NUM_REQUESTS} POST-запросов к контроллеру /quotes
     * и измеряет время выполнения.
     *
     * @throws Exception если выполнение MockMvc запроса завершилось ошибкой
     */
    @Test
    @DisplayName(NUM_REQUESTS + " POST запросов через MockMvc")
    void testRestPostLoad() throws Exception {

        // Засекаем время начала
        long start = System.currentTimeMillis();

        // Отправляем NUM_REQUESTS POST-запросов параллельно
        IntStream.range(0, NUM_REQUESTS).parallel().forEach(i -> {
            try {
                mockMvc.perform(post("/quotes")
                                .content("Quote " + i)
                                .contentType(MediaType.TEXT_PLAIN))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                System.err.println("POST failed for Quote " + i + ": " + e.getMessage());
            }
        });

        // Замеряем длительность теста
        long duration = System.currentTimeMillis() - start;

        // Вывод результатов
        System.out.println("========================================");
        System.out.println("REST @WebMvcTest POST Load Test");
        System.out.println("Total requests: " + NUM_REQUESTS);
        System.out.println("Total time: " + duration + " ms (" + (duration / 1000.0) + " sec)");
        System.out.println("Average time per request: " + (duration / (double) NUM_REQUESTS) + " ms");
        System.out.println("========================================");
    }
}