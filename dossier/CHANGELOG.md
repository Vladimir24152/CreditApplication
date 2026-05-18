# Changelog

## [1.0.0] - 2026-04-04

### Добавлено

#### Инициализация проекта
- Создание Spring Boot 3.5.14 приложения
- Docker Compose конфигурация с Kafka, PostgreSQL, Zookeeper

#### Модули сервисов
- **Deal Service** - управление кредитными заявками и договорами
- **Dossier Service** - отправка email уведомлений и генерация PDF документов
- **Statement Service** - обработка заявок
- **Calculator Service** - расчет кредитных предложений

#### Взаимодействие через Kafka
- Топики для событий: `finish-registration`, `create-documents`, `send-documents`, `send-ses`, `credit-issued`, `statement-denied`
- Производитель (Producer) и потребитель (Consumer) для асинхронной обработки
- Конфигурация `KafkaTopicsProperties` для маппинга тем

#### Email уведомления (Dossier Service)
- `EmailService` - отправка email через SMTP (Gmail)
- `BuildEmailContentService` - построение HTML контента писем через Thymeleaf
- Шаблоны для всех типов уведомлений:
    - Завершение регистрации
    - Создание документов
    - Отправка документов
    - Код подтверждения (SES)
    - Кредит одобрен
    - Заявка отклонена

#### Генерация PDF документов (Dossier Service)
- `PdfGenerationService` - генерация кредитного договора из HTML
- Интеграция с Flying Saucer и OpenPDF
- HTML шаблон кредитного договора со стилями
- Поддержка кириллицы через шрифт DejaVuSans
- Отправка PDF как вложение к email

#### API Endpoints (Deal Service)
- `POST /api/v1/deal/statement` - расчет кредитных предложений
- `POST /api/v1/deal/offer/select` - выбор предложения
- `POST /api/v1/deal/calculate/{statementId}` - завершение регистрации
- `GET /api/v1/deal/{statementId}` - получение данных для договора
- `POST /api/v1/deal/document/{statementId}/send` - отправка документов
- `POST /api/v1/deal/document/{statementId}/sign` - подписание документов
- `POST /api/v1/deal/document/{statementId}/code` - верификация кода

#### HTTP Клиенты
- `DealClient` - REST клиент для взаимодействия Deal → Dossier

#### Валидация
- Аннотации `@Valid` на всех входных DTO
- Паттерны для паспортных данных, email, номера счета
- Валидация даты рождения и кредитных параметров

### Технологический стек
- Java 21
- Spring Boot 3.5.14
- Spring Data JPA
- Spring Kafka
- Spring Mail
- Thymeleaf
- PostgreSQL 15
- Apache Kafka
- Zookeeper
- Lombok
- MapStruct
- Flying Saucer PDF
- OpenPDF
- Swagger/OpenAPI (springdoc)
- JUnit 5, Mockito, Testcontainers

### Зависимости
- Spring Boot: 3.5.14
- Java: 21
- PostgreSQL: 15
- Kafka: 3.9.2
- Flying Saucer PDF: 9.1.22
- Lombok: 1.18.46
- MapStruct: 1.6.3
- Springdoc OpenAPI: 2.2.29