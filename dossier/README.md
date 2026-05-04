# Dossier Service

Микросервис для отправки email уведомлений и генерации PDF документов.

## Описание

Dossier Service отвечает за:
- Отправку email уведомлений клиентам через SMTP
- Генерацию PDF версии кредитного договора
- Построение HTML писем через Thymeleaf шаблоны
- Потребление событий из Kafka
- Отправку писем с вложениями (PDF)

## Архитектура

Микросервис входит в состав системы кредитования и взаимодействует с:
- **Kafka** - получение событий от Deal Service
- **SMTP сервер (Gmail)** - отправка email уведомлений
- **Deal Service** - получение данных для генерации договора (HTTP)

## Технологический стек

- Java 21
- Spring Boot 3.5.14
- Spring Kafka
- Spring Mail
- Thymeleaf
- Flying Saucer PDF
- OpenPDF
- Lombok
- Swagger/OpenAPI (springdoc)
- JUnit 5, Mockito

## Базовый URL
http://localhost:8083

## Swagger UI
http://localhost:8083/swagger-ui.html

## Запуск в Docker

```bash
docker-compose up -d dossier