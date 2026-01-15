# PavelBank 

---

## Технологический стек

- Java
- Spring Boot 3.2.1
- Spring Web (REST + MVC)
- Spring Data JPA
- База данных: **H2 (in-memory)**
- Thymeleaf (UI)
- Тесты: JUnit 5, Spring Boot Test, MockMvc
- Code coverage: JaCoCo

---

## Архитектура проекта

### Основные модули

```
bank_app
├── common        # общие компоненты (ошибки, health, DTO)
├── users         # пользователи
├── accounts      # счета и бизнес-правила
├── transactions  # переводы и история операций
├── audit         # аудит действий
└── ui            # web-интерфейс (Thymeleaf)
```

---

## Общие компоненты (`common`)

Назначение:
- единый формат ошибок
- общие DTO
- health-endpoint

Ключевые файлы:
- `GlobalExceptionHandler` — глобальный обработчик исключений
- `ApiError` — формат ошибки
- `NotFoundException` — 404
- `AmountRequest` — `{ amount }`
- `HealthController` — `/health`

---

## Модуль Users

Отвечает за пользователей системы.

**Основные файлы:**
- `domain/User` — JPA Entity
- `api/UserController` — REST API
- `application/UserRepository` — порт
- `infrastructure/*` — Spring Data реализация

**User:**
- `id`
- `name`
- список счетов

---

## Модуль Accounts (ключевой домен)

Отвечает за счета и **всю бизнес-логику денег**.

### Account (`accounts/domain/Account`)

Поля:
- `id`
- `owner`
- `type`
- `currency`
- `balance`
- `debtAmount`
- `blocked`
- `blockReason`
- `depositEndDate`
- `version` (locking)

### Бизнес-правила

#### Списание (`debit`)
- запрещено, если счет заблокирован
- запрещено с депозитного счета до `depositEndDate`
- запрещено при недостатке средств

#### Зачисление (`credit`)
- запрещено, если счет заблокирован **не по причине DEBT**
- при блокировке `DEBT` — разрешено (для погашения долга)

#### Долг
- `markDebt(amount)`
  - устанавливает долг
  - блокирует счет причиной `DEBT`
  - депозитные счета не могут иметь долг
- `repayDebt(amount)`
  - уменьшает долг
  - переплата возвращается на баланс
  - долг не уходит в минус
  - при полном погашении блокировка снимается

#### Блокировки
- `block(reason)` — любая причина, кроме `DEBT`
- `unblock()` — запрещено, если `DEBT` и долг > 0

---

## Переводы и транзакционность

### Пессимистическая блокировка

Для переводов используется `PESSIMISTIC_WRITE`.

Ключевая идея:
- оба счета блокируются **в детерминированном порядке по UUID**
- это исключает дедлоки при конкурентных переводах

Реализация:
- `AccountJpaRepository.findByIdForUpdate`
- `AccountOperationService.transfer`

### TransferMoneyUseCase

`transactions/application/TransferMoneyUseCase`

В рамках одной транзакции:
1. Блокирует счета
2. Проверяет валюту
3. Выполняет `debit` и `credit`
4. Сохраняет `Transaction`
5. Записывает `AuditLog`

---

## Модуль Transactions

Отвечает за историю операций.

**Файлы:**
- `domain/Transaction`
- `application/TransferMoneyUseCase`
- `application/TransactionQueryService`
- `api/TransactionController`

Transaction:
- `fromAccountId`
- `toAccountId`
- `amount`
- `currency`
- `createdAt`

---

## Модуль Audit

Простой аудит действий.

- `AuditLog` — entity
- `AuditService` — запись событий
- используется при переводах

---

## REST API

### Health
```
GET /health
```

---

### Users API

```
POST /api/users
GET  /api/users
GET  /api/users/{id}
```

---

### Accounts API

```
POST /api/accounts
GET  /api/accounts/{id}
GET  /api/accounts?userId=...
POST /api/accounts/{id}/block
POST /api/accounts/{id}/unblock
```

#### Денежные операции
```
POST /api/accounts/{id}/deposit
POST /api/accounts/{id}/withdraw
```

#### Долг
```
POST /api/accounts/{id}/debt
POST /api/accounts/{id}/repay-debt
```

---

### Transactions API

```
POST /api/transactions/transfer
GET  /api/transactions/{id}
GET  /api/transactions?accountId=...
```

---

## Ошибки

Обрабатываются глобально через `GlobalExceptionHandler`.

Типовые ответы:
- `404 NOT FOUND`
- `400 BAD REQUEST`
- `409 CONFLICT`

Формат:
```json
{
  "code": "CONFLICT",
  "message": "Account blocked",
  "path": "/api/accounts/{id}/withdraw",
  "timestamp": "..."
}
```

---

## UI (Thymeleaf)

Основные страницы:
- `/ui/login`
- `/ui/dashboard`
- `/ui/accounts/{id}`

Поддерживаются действия:
- создание пользователя
- создание счета
- deposit / withdraw
- transfer
- block / unblock
- debt / repay debt

---

## Тестирование

### Типы тестов

#### Domain unit tests
- `AccountTest` — бизнес-правила счета

#### Application / Use case tests
- `AccountOperationServiceTest`
- `TransferMoneyUseCaseTest`
- `AccountDebtServiceTest`
- `TransactionQueryServiceTest`

#### Infrastructure tests
- JPA-репозитории

#### Integration tests
- `ApiIntegrationTest`
- `UiIntegrationTest`

#### e2e tests
- `ApiE2ETest`