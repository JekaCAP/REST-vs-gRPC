package ru.testing.evgeniy.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <h1>REST-контроллер для работы с цитатами</h1>
 *
 * <p>
 * Этот контроллер предоставляет простой REST API для добавления и получения цитат.
 * Реализован с использованием Spring Boot и аннотаций {@link RestController} и {@link RequestMapping}.
 * </p>
 *
 * <h2>Особенности реализации:</h2>
 * <ul>
 *     <li>Список цитат {@link CopyOnWriteArrayList} потокобезопасен при одновременном добавлении и чтении.</li>
 *     <li>Методы поддерживают простую работу с JSON: {@link @PostMapping} принимает текст цитаты в теле запроса,
 *     {@link @GetMapping("/random")} возвращает случайную цитату.</li>
 *     <li>API является синхронным: каждый HTTP-запрос обрабатывается в одном потоке сервера и возвращает ответ сразу.</li>
 * </ul>
 *
 * <h2>Рекомендации и примечания:</h2>
 * <ul>
 *     <li>Для масштабируемости в будущем можно заменить {@link CopyOnWriteArrayList} на
 *         более производительные структуры данных, например {@link java.util.concurrent.ConcurrentLinkedQueue}.</li>
 *     <li>Методы можно расширять дополнительной логикой валидации запросов, логирования или интеграции с БД.</li>
 *     <li>REST-контроллер удобен для интеграции с браузером, Postman, фронтендом или другими HTTP-клиентами.</li>
 * </ul>
 *
 * <h2>Примеры использования:</h2>
 * <pre>{@code
 * // Добавление цитаты через POST
 * POST /quotes
 * Body: "Hello world"
 * Response: "Hello world"
 *
 * // Получение случайной цитаты через GET
 * GET /quotes/random
 * Response: "Hello world" // если это единственная цитата
 * }</pre>
 */
@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteRestController {

    /**
     * Потокобезопасный список для хранения цитат
     */
    private final List<String> quotes = new CopyOnWriteArrayList<>();

    /**
     * Добавляет новую цитату.
     *
     * @param quote текст цитаты, переданный в теле POST-запроса
     * @return добавленная цитата (подтверждение успешного добавления)
     */
    @PostMapping
    public String addQuote(@RequestBody String quote) {
        quotes.add(quote);
        return quote;
    }

    /**
     * Возвращает случайную цитату из списка.
     *
     * @return случайная цитата или "No quotes", если список пуст
     */
    @GetMapping("/random")
    public String getRandomQuote() {
        return quotes.isEmpty() ? "No quotes" :
                quotes.get(ThreadLocalRandom.current().nextInt(quotes.size()));
    }
}