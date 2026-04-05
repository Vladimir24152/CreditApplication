# Credit Calculator Microservice

Микросервис для расчета кредитных предложений и параметров кредита.

## Описание

Credit Calculator отвечает за:
- Расчет кредитных предложений на основе первичных данных клиента
- Скоринг клиента с полными данными
- Расчет полной стоимости кредита (ПСК)
- Формирование графика ежемесячных платежей

## Архитектура

Микросервис входит в состав системы кредитования и взаимодействует с:
- **МС Заявка** - получение запросов на расчет
- **МС Сделка** - передача рассчитанных параметров кредита

##  Технологический стек

- Java 21
- Spring Boot 3.4.4
- Maven
- Lombok
- Swagger/OpenAPI 3.0 (springdoc)
- JUnit 5

##  Базовый URL
http://localhost:8080
##  Swagger UI
http://localhost:8080/swagger-ui

##  Запуск в Docker
docker-compose up -d

##  Endpoints
### 1. POST /calculator/offers 
- Расчет кредитных предложений
###  2. POST /calculator/calc 
- Скоринг клиента с полными данными
- Расчет полной стоимости кредита (ПСК)
- Формирование графика ежемесячных платежей

## Конфигурация
### Сервер
server.port=8080

### Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui
springdoc.swagger-ui.url=/v3/api-docs

### Логирование
logging.level.org.springdoc=DEBUG
logging.level.org.neoflex.calculator=DEBUG

### Базовая ставка кредита (%)
loan.calculator.base-rate=15.0

### Страховка
loan.calculator.insurance-cost-percent=2.0      # Стоимость страховки (% от суммы)
loan.calculator.insurance-rate-discount=3.0     # Скидка за страховку (%)

### Зарплатный клиент
loan.calculator.salary-client-discount=1.0      # Скидка для зарплатного клиента (%)

### Надбавки за статус занятости
loan.calculator.self-employ-rate-add=2.0        # Надбавка для самозанятых (%)
loan.calculator.business-owner-rate-add=1.0     # Надбавка для владельцев бизнеса (%)

### Скидки за должность
loan.calculator.mid-manager-rate-discount=2.0   # Скидка для мидл-менеджера (%)
loan.calculator.top-manager-rate-discount=3.0   # Скидка для топ-менеджера (%)

### Семейное положение
loan.calculator.married-rate-discount=3.0       # Скидка для женатых/замужем (%)
loan.calculator.divorced-rate-add=1.0           # Надбавка для разведенных (%)

### Пол и возраст
loan.calculator.male-rate-discount=3.0          # Скидка для мужчин 30-55 лет (%)
loan.calculator.female-rate-discount=3.0        # Скидка для женщин 32-60 лет (%)
loan.calculator.not-binary-rate-add=7.0         # Надбавка для небинарных персон (%)