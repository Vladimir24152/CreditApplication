# Deal Service Microservice

Микросервис для управления кредитными заявками.

## Описание

Statement Service отвечает за:

- Создание и управление кредитными заявками
- Прескоринг данных клиента

## Архитектура

Микросервис входит в состав системы кредитования и взаимодействует с:

- **МС Сделака** - получение кредитных предложений и полного расчета

## Технологический стек

- Java 21
- Spring Boot 3.4.4
- Lombok
- Swagger/OpenAPI 3.0 (springdoc)
- JUnit 5, Mockito

## Базовый URL

http://localhost:8082

## Swagger UI

http://localhost:8082/swagger-ui

## Запуск в Docker

```bash
docker-compose up -d