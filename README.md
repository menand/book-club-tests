# Book Club API Test Automation Framework

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/)
[![JUnit](https://img.shields.io/badge/Test_Framework-JUnit_6-green.svg)](https://junit.org/junit5/)
[![RestAssured](https://img.shields.io/badge/API_Testing-RestAssured_6-orange.svg)](https://rest-assured.io/)
[![Allure](https://img.shields.io/badge/Reporting-Allure-purple.svg)](https://docs.qameta.io/allure/)

Фреймворк для автоматизированного тестирования API сервиса Book Club. Покрытие: аутентификация, регистрация, управление профилями, книжные клубы.

## Архитектура

### API-клиенты (Page Object Model для API)

`ApiClient` — единая точка доступа к эндпоинт-клиентам:

- `AuthApiClient` — аутентификация (`/auth/token/`, `/auth/logout/`)
- `UsersApiClient` — пользователи (`/users/register/`, `/users/me/`)
- `ClubsApiClient` — клубы (`/clubs/`)

Каждый клиент инкапсулирует все сценарии (позитивные и негативные) для своего эндпоинта. Тесты не используют `given()` напрямую — только через клиенты.

### Спецификации (Specs)

Переиспользуемые настройки запросов и валидации ответов:

- `BaseSpec` — базовый `RequestSpecification` (basePath `/api/v1`, JSON, Allure-фильтр)
- `UserSpec.authRequestSpec(token)` — подстановка `Authorization: Bearer`
- `LoginSpec`, `RegistrationSpec`, `LogoutSpec`, `ClubsSpec`, `UserSpec` — response-спеки с JSON Schema валидацией

### Модели данных

Все модели — **Java records**. Единый стиль accessors без геттеров: `response.access()`, `user.firstName()`.

```java
public record LoginBodyModel(String username, String password) {}
```

`UpdateUserModel` использует `@JsonInclude(NON_NULL)` для PATCH-запросов.

### Валидация ответов

- JSON Schema валидация через `json-schema-validator` в response-спеках
- AssertJ для бизнес-проверок в тестах

### Отчетность

Allure с кастомными FreeMarker-шаблонами (`resources/tpl/`) для запросов/ответов.

## Структура проекта

```
src/test/
├── java/
│   ├── api/                # API-клиенты (ApiClient, AuthApiClient, UsersApiClient, ClubsApiClient)
│   ├── allure/             # CustomAllureListener
│   ├── models/             # Java records (общий ValidationErrorResponseModel)
│   ├── specs/              # Request/Response спецификации
│   │   ├── BaseSpec
│   │   ├── login/LoginSpec
│   │   ├── registration/RegistrationSpec
│   │   ├── logout/LogoutSpec
│   │   ├── clubs/ClubsSpec
│   │   └── users/UserSpec
│   └── tests/              # Тестовые классы (extend TestBase)
└── resources/
    ├── schemas/            # JSON Schema для валидации ответов
    ├── tpl/                # FreeMarker-шаблоны для Allure
    └── junit-platform.properties
```

## Зависимости

- **Тестирование:** JUnit 6, RestAssured 6, AssertJ
- **Валидация:** JSON Schema Validator
- **Отчетность:** Allure
- **Утилиты:** Jackson
- **Форматирование:** Spotless (Google Java Format AOSP, без wildcard-импортов)

## Требования

- Java 21+
- Git

## Запуск тестов

```bash
./gradlew test                                    # все тесты (параллельно по классам)
./gradlew test --tests LoginTests                 # один класс
./gradlew test -Dgroups=smoke,regression          # по тегам
./gradlew spotlessApply                           # автоформатирование
./gradlew spotlessCheck                           # проверка форматирования
./gradlew allureServe                             # Allure-отчет на :19432
```

### Параллельный запуск

Тесты выполняются параллельно на уровне классов (`parallelism=4`). Методы внутри класса идут последовательно. Конфигурация: `src/test/resources/junit-platform.properties`.

Для динамических данных (регистрация) используется `UUID.randomUUID()` вместо `System.currentTimeMillis()` для потокобезопасности.

## Конфигурация

| Параметр | Описание | По умолчанию |
|---------|---------|-------------|
| `groups` | Теги для запуска тестов | — |
| `allure.results.directory` | Директория результатов Allure | `build/allure-results` |

## Написание тестов

### Новый тест

```java
class NewFeatureTests extends TestBase {

    @Test
    @Description("Описание теста")
    void testScenario() {
        SuccessfulLoginResponseModel response =
                api.auth.login(new LoginBodyModel("user", "pass"));

        assertThat(response.access()).startsWith("eyJ");
    }
}
```

### Новая модель

```java
public record NewModel(String field1, Integer field2) {}
```

### Новая спецификация

```java
public class NewSpec {
    public static RequestSpecification newRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(matchesJsonSchemaInClasspath("schemas/new/response_schema.json"))
                    .build();
}
```

### Конвенции

- Модели — только records, без Lombok
- Импорты — только явные, без `import foo.*` (Spotless блокирует билд)
- Все запросы к API — через клиенты, не через `given()` напрямую
- Авторизация — через `UserSpec.authRequestSpec(token)`, не через ручной header
- Одноразовые body-объекты передаются инлайн: `api.auth.login(new LoginBodyModel(...))`
