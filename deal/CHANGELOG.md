## **CHANGELOG.md**

# Changelog

## [1.0.0] - 2026-04-01

### Добавлено

#### Инициализация проекта
- Создание Spring Boot 3.4.4 приложения
- Модульная структура проекта с модулем `deal`
- Docker Compose конфигурация с PostgreSQL

#### Сущности базы данных
- `Client` - сущность клиента с полями в формате JSONB
- `Statement` - сущность заявки на кредит с историей статусов
- `Credit` - сущность кредитного договора с графиком платежей
- Поддержка JSONB для сложных полей (паспорт, трудоустройство, история статусов, график платежей)

#### DTO объекты
- `LoanStatementRequestDto` - запрос на предварительный расчет
- `LoanOfferDto` - кредитное предложение с условиями
- `FinishRegistrationRequestDto` - завершение регистрации клиента
- `CreditDto` - полный расчет кредита
- `EmploymentDto` - информация о трудоустройстве
- `PaymentScheduleElementDto` - элемент графика платежей
- `HttpErrorResponse` - ответ с ошибкой
- `HttpErrorInternalServiceResponse` - ответ с ошибкой внутреннего сервиса

#### API Endpoints
- `POST /api/v1/deal/statement` - расчет кредитных предложений
- `POST /api/v1/deal/offer/select` - выбор кредитного предложения
- `POST /api/v1/deal/calculate/{statementId}` - завершение регистрации и расчет кредита

#### Бизнес-логика
- **Сервис заявок (StatementService):**
  - Создание и сохранение клиента в БД
  - Создание заявки со связью с клиентом
  - Интеграция с CalculatorClientService для получения предложений
  - Выбор кредитного предложения с обновлением статуса заявки
  - Ведение истории статусов заявки

- **Сервис кредитов (CreditService):**
  - Получение заявки по идентификатору
  - Формирование ScoringDataDto из данных клиента и запроса
  - Интеграция с CalculatorClientService для расчета кредита
  - Создание и сохранение сущности Credit со статусом CALCULATED
  - Обновление статуса заявки и истории статусов

#### Интеграция с микросервисами
- **CalculatorClientService** - REST клиент для взаимодействия с МС Калькулятор:
  - `calculateLoanOffers` - получение кредитных предложений
  - `calculateCredit` - полный расчет кредита

#### Мапперы
- `ClientMapper` - преобразование LoanStatementRequestDto → Client
- `StatementMapper` - преобразование Client → Statement
- `CreditMapper` - преобразование CreditDto → Credit

#### Документация
- Swagger/OpenAPI документация для всех endpoints
- Аннотации `@Schema` для всех DTO
- Примеры ответов для различных HTTP статусов (200, 400, 404, 500)

#### Логирование
- INFO уровень для входящих запросов и результатов
- DEBUG уровень для промежуточных операций
- `LoggingInterceptor` для логирования всех HTTP запросов

#### Обработка ошибок
- `GlobalExceptionHandler` с перехватом всех исключений
- Кастомные исключения: `NotValidBirthDateException`, `ScoringFailedException`
- Корректные HTTP статусы для различных ошибок

#### Тестирование
- **Unit-тесты:**
  - `StatementServiceTest` - тестирование логики работы с заявками
  - `CreditServiceTest` - тестирование логики расчета кредита
  - Покрытие > 90% для сервисов

- **Интеграционные тесты:**
  - `DealControllerTest` - тестирование всех endpoints

#### Конфигурация
- `application.yml` с настройками:
  - Подключение к PostgreSQL
  - Настройки JPA/Hibernate

- `docker-compose.yml` для локального запуска:
  - PostgreSQL (порт 5432)


#### Валидация
- Аннотации `@Valid` на всех входных DTO
- Кастомные валидаторы для даты рождения
- Паттерны для email, паспортных данных, номера счета

### Зависимости
- Spring Boot: 3.4.4
- Java: 21
- PostgreSQL: 15
- Testcontainers: 1.19.8
- Lombok: 1.18.36
- MapStruct: 1.6.3
- Springdoc OpenAPI: 2.8.5
