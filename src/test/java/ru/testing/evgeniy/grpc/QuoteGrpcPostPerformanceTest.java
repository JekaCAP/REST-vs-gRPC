package ru.testing.evgeniy.grpc;

import com.example.quotes.grpc.QuoteRequest;
import com.example.quotes.grpc.QuoteResponse;
import com.example.quotes.grpc.QuoteServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * Автономный нагрузочный тест gRPC сервиса цитат (addQuote).
 *
 * <p>Тест демонстрирует:
 * <ul>
 *     <li>Использование in-process gRPC сервера для автономного тестирования без реального сетевого соединения.</li>
 *     <li>Асинхронную отправку большого количества gRPC-запросов.</li>
 *     <li>Замер времени обработки N запросов и вычисление средней задержки на запрос.</li>
 * </ul>
 *
 * <p>Константа {@link #NUM_REQUESTS} задает количество POST-запросов для теста.
 *
 * <p>Для запуска теста синхронно (т.е. блокирующие вызовы) необходимо заменить
 * асинхронный stub на блокирующий. Для этого:
 * <pre>
 * // Вместо:
 * var stub = QuoteServiceGrpc.newStub(channel);
 * // используйте:
 * var stub = QuoteServiceGrpc.newBlockingStub(channel);
 * </pre>
 * После этого метод <code>addQuote</code> будет блокировать поток до получения ответа от сервиса,
 * и <code>CountDownLatch</code> уже не нужен.
 *
 * <p>Пример замены асинхронной отправки на синхронную:
 * <pre>
 * IntStream.range(0, NUM_REQUESTS).forEach(i ->
 *     stub.addQuote(QuoteRequest.newBuilder().setText("Quote " + i).build())
 * );
 * </pre>
 */
@DisplayName("Автономный gRPC нагрузочный тест")
public class QuoteGrpcPostPerformanceTest {

    /**
     * Количество POST-запросов для теста
     */
    private static final int NUM_REQUESTS = 10000;

    /**
     * Встроенный in-memory gRPC сервис для тестирования.
     * <p>Хранит цитаты в потоко-безопасном списке и отвечает на addQuote.
     */
    private static class InMemoryQuoteService extends QuoteServiceGrpc.QuoteServiceImplBase {
        private final List<String> quotes = new CopyOnWriteArrayList<>();

        @Override
        public void addQuote(QuoteRequest request, StreamObserver<QuoteResponse> responseObserver) {
            // Добавляем цитату в список
            quotes.add(request.getText());
            // Отправляем ответ клиенту
            responseObserver.onNext(QuoteResponse.newBuilder().setText(request.getText()).build());
            responseObserver.onCompleted();
        }
    }

    /**
     * Асинхронный нагрузочный тест: отправляет {@link #NUM_REQUESTS} gRPC POST-запросов
     * к in-process серверу и измеряет время выполнения.
     *
     * @throws InterruptedException если поток теста был прерван во время ожидания завершения всех запросов
     */
    @Test
    @DisplayName(NUM_REQUESTS + " POST запросов к gRPC in-process серверу")
    void testGrpcAddQuoteLoad() throws InterruptedException {
        // Генерируем уникальное имя in-process сервера
        String serverName = InProcessServerBuilder.generateName();
        // Латч для ожидания завершения всех асинхронных вызовов
        CountDownLatch latch = new CountDownLatch(NUM_REQUESTS);

        // Поднимаем in-process сервер
        var server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()          // Используем прямой executor для синхронного выполнения задач
                .addService(new InMemoryQuoteService())
                .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Подключаемся к in-process серверу через асинхронный stub
        ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        var stub = QuoteServiceGrpc.newStub(channel); // <-- заменить на newBlockingStub(channel) для синхронного теста

        // Засекаем время выполнения
        long start = System.currentTimeMillis();

        // Асинхронная отправка NUM_REQUESTS gRPC addQuote
        IntStream.range(0, NUM_REQUESTS).forEach(i ->
                stub.addQuote(QuoteRequest.newBuilder().setText("Quote " + i).build(), new StreamObserver<>() {
                    @Override
                    public void onNext(QuoteResponse value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                })
        );

        latch.await(); // Ждём завершения всех асинхронных запросов
        long duration = System.currentTimeMillis() - start;

        // Завершаем соединение
        channel.shutdown();
        server.shutdown();

        // Вывод результатов
        System.out.println("========================================");
        System.out.println("gRPC POST Test");
        System.out.println("Total requests: " + NUM_REQUESTS);
        System.out.println("Total time: " + duration + " ms (" + (duration / 1000.0) + " sec)");
        System.out.println("Average time per request: " + (duration / (double) NUM_REQUESTS) + " ms");
        System.out.println("========================================");
    }
}