package ru.testing.evgeniy.grpc;

import com.example.quotes.grpc.QuoteRequest;
import com.example.quotes.grpc.QuoteResponse;
import com.example.quotes.grpc.QuoteServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <h1>gRPC сервис для работы с цитатами</h1>
 *
 * <p>
 * Этот сервис реализует интерфейс {@link QuoteServiceGrpc.QuoteServiceImplBase}, сгенерированный из
 * Protocol Buffers (.proto). Позволяет добавлять новые цитаты и получать случайную цитату.
 * </p>
 *
 * <h2>Особенности реализации:</h2>
 * <ul>
 *     <li>Используется {@link CopyOnWriteArrayList} для хранения цитат — обеспечивает потокобезопасность при
 *     одновременном добавлении и чтении.</li>
 *     <li>Методы реализованы в стиле gRPC: <strong>асинхронное</strong> взаимодействие через
 *     {@link StreamObserver}.</li>
 *     <li>Метод {@link #getRandomQuote(Empty, StreamObserver)} выбирает случайную цитату,
 *     используя {@link ThreadLocalRandom}, что безопасно для многопоточной среды.</li>
 *     <li>Сервис поддерживает как синхронное (через блокирующий stub), так и асинхронное (через async stub)
 *     взаимодействие с клиентом.</li>
 * </ul>
 *
 * <h2>Рекомендации и примечания:</h2>
 * <ul>
 *     <li>Если вы хотите, чтобы все вызовы были строго <strong>синхронными</strong>, используйте
 *     блокирующий stub на стороне клиента, например {@code QuoteServiceGrpc.newBlockingStub(channel)}.</li>
 *     <li>Для поддержки масштабируемости и потокобезопасности в будущем можно заменить
 *     {@link CopyOnWriteArrayList} на более производительные структуры данных с ограниченной блокировкой,
 *     например {@link java.util.concurrent.ConcurrentLinkedQueue} или специализированный кеш.</li>
 *     <li>Методы можно расширять дополнительной логикой валидации запросов, логирования или интеграции с БД.</li>
 * </ul>
 *
 * <h2>Примеры использования:</h2>
 * <pre>{@code
 * // Асинхронный вызов
 * stub.addQuote(QuoteRequest.newBuilder().setText("Hello").build(), new StreamObserver<QuoteResponse>() {
 *     @Override
 *     public void onNext(QuoteResponse value) { System.out.println("Added: " + value.getText()); }
 *     @Override
 *     public void onError(Throwable t) { t.printStackTrace(); }
 *     @Override
 *     public void onCompleted() { System.out.println("Request completed"); }
 * });
 *
 * // Синхронный вызов через блокирующий stub
 * QuoteResponse response = blockingStub.getRandomQuote(Empty.newBuilder().build());
 * System.out.println("Random quote: " + response.getText());
 * }</pre>
 */
@GrpcService
public class QuoteGrpcService extends QuoteServiceGrpc.QuoteServiceImplBase {

    /**
     * Потокобезопасный список для хранения цитат
     */
    private final List<String> quotes = new CopyOnWriteArrayList<>();

    /**
     * Добавляет новую цитату в сервис.
     *
     * @param request          объект {@link QuoteRequest}, содержащий текст цитаты
     * @param responseObserver асинхронный колбэк {@link StreamObserver}, через который
     *                         отправляется подтверждение добавления цитаты клиенту
     */
    @Override
    public void addQuote(QuoteRequest request, StreamObserver<QuoteResponse> responseObserver) {
        quotes.add(request.getText());

        QuoteResponse response = QuoteResponse.newBuilder()
                .setText(request.getText())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Возвращает случайную цитату из списка.
     *
     * @param request          пустой объект {@link Empty} (не содержит данных)
     * @param responseObserver асинхронный колбэк {@link StreamObserver}, через который
     *                         отправляется случайная цитата клиенту
     */
    @Override
    public void getRandomQuote(Empty request, StreamObserver<QuoteResponse> responseObserver) {
        String result = quotes.isEmpty() ? "No quotes" :
                quotes.get(ThreadLocalRandom.current().nextInt(quotes.size()));

        QuoteResponse response = QuoteResponse.newBuilder()
                .setText(result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
