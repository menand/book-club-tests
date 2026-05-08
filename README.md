# Book Club API Test Automation Framework

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/)
[![JUnit](https://img.shields.io/badge/Test_Framework-JUnit_6-green.svg)](https://junit.org/junit5/)
[![RestAssured](https://img.shields.io/badge/API_Testing-RestAssured_6-orange.svg)](https://rest-assured.io/)
[![Allure](https://img.shields.io/badge/Reporting-Allure-purple.svg)](https://docs.qameta.io/allure/)

Фреймворк для автоматизированного тестирования API сервиса Book Club. Покрытие: аутентификация, регистрация, управление профилями, CRUD клубов и рецензий, членство в клубах.

## Архитектура

### API-клиенты (Page Object Model для API)

`ApiClient` — единая точка доступа к эндпоинт-клиентам:

- `AuthApiClient` — аутентификация (`/auth/token/`, `/auth/logout/`)
- `UsersApiClient` — пользователи (`/users/register/`, `/users/me/`, в т.ч. `DELETE /users/me/`)
- `ClubsApiClient` — клубы (`/clubs/`, `/clubs/{id}/`, `/clubs/{id}/members/me/`, `/clubs/reviews/`)

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

Модели для PATCH (`UpdateUserModel`, `UpdateClubBodyModel`, `UpdateReviewBodyModel`) используют `@JsonInclude(NON_NULL)` — поля со значением `null` не сериализуются в JSON.

### Валидация ответов

- JSON Schema валидация через `json-schema-validator` в response-спеках
- AssertJ для бизнес-проверок в тестах
- Каждая проверка обёрнута в `Allure.step("...", () -> assertThat(...))` для гранулярного отображения в отчёте

### Отчётность

Allure с кастомными FreeMarker-шаблонами (`resources/tpl/`) для запросов/ответов.

## Структура проекта

```
src/test/
├── java/
│   ├── api/                # API-клиенты (ApiClient, AuthApiClient, UsersApiClient, ClubsApiClient)
│   ├── allure/             # CustomAllureListener
│   ├── models/             # Java records
│   │   ├── ValidationErrorResponseModel
│   │   ├── login/, logout/, registration/, users/
│   │   └── clubs/          # Club*, *Review*, Create/Update*Body, ReviewsListResponse
│   ├── specs/              # Request/Response спецификации
│   │   ├── BaseSpec
│   │   ├── login/LoginSpec
│   │   ├── registration/RegistrationSpec
│   │   ├── logout/LogoutSpec
│   │   ├── clubs/ClubsSpec
│   │   └── users/UserSpec
│   └── tests/              # Тестовые классы (extend TestBase)
│       ├── LoginTests, LogoutTests, RegistrationTests, UpdateUserTests
│       ├── ClubsTests              # GET /clubs/ (список)
│       ├── ClubsCrudTests          # POST/GET/PUT/PATCH/DELETE /clubs/{id}/
│       ├── ClubReviewsCrudTests    # CRUD /clubs/reviews/
│       ├── ClubMembersTests        # join/leave /clubs/{id}/members/me/
│       ├── ClubFixtures            # фабрика тестовых данных для клубов
│       └── TestData                # константы (учётка для LoginTests)
└── resources/
    ├── schemas/clubs/      # JSON Schema: clubs_list, club, review, reviews_list
    ├── schemas/login/, registration/, users/
    ├── tpl/                # FreeMarker-шаблоны для Allure
    └── junit-platform.properties
```

## Зависимости

- **Тестирование:** JUnit 6, RestAssured 6, AssertJ
- **Валидация:** JSON Schema Validator
- **Отчётность:** Allure
- **Утилиты:** Jackson
- **Форматирование:** Spotless с **palantir-java-format** (без wildcard-импортов)

## Требования

- Java 21+
- Git

## Запуск тестов

```bash
./gradlew test                                    # все тесты (параллельно по классам)
./gradlew test --tests LoginTests                 # один класс
./gradlew test --tests '*loginWithEmpty*'         # по паттерну
./gradlew test -Dgroups=SMOKE                     # по тегам
./gradlew test -Dgroups=LOGIN,REGRESS             # несколько тегов (OR)
./gradlew spotlessApply                           # автоформатирование
./gradlew spotlessCheck                           # проверка форматирования (часть `./gradlew check`)
./gradlew allureServe                             # Allure-отчёт на :19432
./gradlew clean test                              # чистый прогон
```

> **Внимание:** `spotlessApply` **не** запускается автоматически перед `test`. Если форматирование сбито — `./gradlew test` пройдёт, но `./gradlew check` упадёт. Запустите `./gradlew spotlessApply` вручную перед коммитом.

### Параллельный запуск

Тесты выполняются параллельно на уровне классов (`parallelism=4`). Методы внутри класса идут последовательно. Конфигурация: `src/test/resources/junit-platform.properties`.

Для уникальных данных (регистрация, клубы) используется `UUID.randomUUID()` вместо `System.currentTimeMillis()` для потокобезопасности.

### Очистка тестовых данных

Тесты, создающие пользователей в `@BeforeEach`, обязаны удалять их в `@AfterEach` через `api.users.deleteCurrentUser(token)`. Backend каскадно удаляет связанные клубы и рецензии. Это держит стенд чистым.

## Конфигурация

| Параметр | Описание | По умолчанию |
|---------|---------|-------------|
| `groups` | Теги для запуска тестов (см. `@Tag`) | — |
| `api.baseUri` | Базовый URI API | `https://book-club.qa.guru` |
| `api.basePath` | Базовый путь API | `/api/v1` |
| `allure.results.directory` | Директория результатов Allure | `build/allure-results` |

Пример запуска против другого окружения:
```bash
./gradlew test -Dapi.baseUri=https://staging.book-club.example.com
```

## Написание тестов

### Новый тест

```java
class NewFeatureTests extends TestBase {

    @Test
    @Description("Описание теста")
    void testScenario() {
        SuccessfulLoginResponseModel response =
                api.auth.login(new LoginBodyModel("user", "pass"));

        step("Проверки", () -> {
            step("access начинается с JWT-префикса",
                    () -> assertThat(response.access()).startsWith("eyJ"));
        });
    }
}
```

### Новая модель

```java
public record NewModel(String field1, Integer field2) {}
```

Для PATCH-моделей (опциональные поля):
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatchModel(String field1, Integer field2) {}
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
- Каждый публичный метод ApiClient помечается `@Step("...")`
- Каждый позитивный response-spec валидирует JSON Schema; для 204/401/404 достаточно проверки статуса
- Тесты, создающие юзеров, обязаны удалять их в `@AfterEach`
- Assertions в тестах оборачиваются в `Allure.step("...", () -> ...)` для отображения в отчёте
- Одноразовые body-объекты передаются инлайн: `api.auth.login(new LoginBodyModel(...))`
- Уникальные данные через `UUID.randomUUID().toString().substring(0, 8)`
