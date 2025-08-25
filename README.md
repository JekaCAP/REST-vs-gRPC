# REST vs gRPC

Мини-проект для демонстрации различий между REST и gRPC на Java + Spring Boot.

## Описание проекта

Проект включает два способа работы с цитатами:

1. **REST API** (`QuoteRestController`)
   - `POST /quotes` — добавление новой цитаты
   - `GET /quotes/random` — получение случайной цитаты
2. **gRPC API** (`QuoteGrpcService`)
   - `AddQuote(QuoteRequest) -> QuoteResponse` — добавление новой цитаты
   - `GetRandomQuote(Empty) -> QuoteResponse` — получение случайной цитаты

Цель проекта:
- Показать разницу в производительности и типизации между REST и gRPC.
- Демонстрация синхронного и асинхронного взаимодействия.
- Сравнение сетевого трафика JSON vs Protobuf.
- Возможность проводить нагрузочные тесты с помощью MockMvc и gRPC clients.

---

## Технологии

- Java 17+
- Spring Boot 3
- gRPC + Protobuf
- Gradle
- JUnit 5
- Lombok

REST API доступен по адресу:
http://localhost:8080/quotes
http://localhost:8080/quotes/random

gRPC сервер доступен на порту:
localhost:9090 (по умолчанию)
