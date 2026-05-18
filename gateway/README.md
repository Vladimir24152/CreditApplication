# Gateway Service

API Gateway для маршрутизации запросов к микросервисам кредитного приложения.

## Endpoints

| Метод | Путь | Назначение |
|-------|------|------------|
| POST | `/api/v1/deal/statement` | Расчет кредитных предложений |
| POST | `/api/v1/deal/offer/select` | Выбор кредитного предложения |
| POST | `/api/v1/deal/calculate/{statementId}` | Завершение регистрации |
| POST | `/api/v1/deal/document/{statementId}/send` | Отправка документов |
| POST | `/api/v1/deal/document/{statementId}/sign` | Подписание документов |
| POST | `/api/v1/deal/document/{statementId}/code` | Верификация SES кода |
| GET | `/api/v1/deal/document/{statementId}` | Информация для документов |
| GET | `/api/v1/deal/admin/statement/{statementId}` | Получение заявки по ID |
| GET | `/api/v1/deal/admin/statement` | Получение всех заявок |
| POST | `/api/v1/statement` | Расчет предложений |
| POST | `/api/v1/statement/offer` | Выбор предложения |

## Swagger UI

`http://localhost:8084/swagger-ui.html`

