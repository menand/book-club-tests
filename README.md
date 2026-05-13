# Book Club API + UI Test Automation Framework

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/)
[![JUnit](https://img.shields.io/badge/Test_Framework-JUnit_6-green.svg)](https://junit.org/junit5/)
[![RestAssured](https://img.shields.io/badge/API_Testing-RestAssured_6-orange.svg)](https://rest-assured.io/)
[![Selenide](https://img.shields.io/badge/UI_Testing-Selenide_7-red.svg)](https://selenide.org/)
[![Allure](https://img.shields.io/badge/Reporting-Allure-purple.svg)](https://docs.qameta.io/allure/)

Фреймворк для автоматизированного тестирования сервиса Book Club: API (REST Assured) и UI (Selenide). Покрытие — аутентификация, регистрация, профиль, CRUD клубов и рецензий, членство, права доступа.

**Live**: API — `https://book-club.qa.guru/api/v1`, UI — `https://book-club.qa.guru/` (Vue 3 SPA).

## Архитектура

### API-клиенты (Page Object Model для API)

`ApiClient` — единая точка доступа к эндпоинт-клиентам:

- `AuthApiClient` — аутентификация (`/auth/token/`, `/auth/logout/`)
- `UsersApiClient` — пользователи (`/users/register/`, `/users/me/` с `DELETE`)
- `ClubsApiClient` — клубы (`/clubs/`, `/clubs/{id}/`, `/clubs/{id}/members/me/`, `/clubs/reviews/`)

Каждый клиент инкапсулирует и позитивные, и негативные (401/403/404/400/405/415) сценарии. Тесты не используют `given()` напрямую — только через клиенты.

### Спецификации (Specs)

Переиспользуемые настройки запросов и валидации ответов:

- `BaseSpec.baseRequestSpec` — базовый `RequestSpecification` (basePath `/api/v1`, JSON, Allure-фильтр).
- `UserSpec.authRequestSpec(token)` — подстановка `Authorization: Bearer`.
- `*Spec` — `ResponseSpec` по доменам. Все JSON-Schema-спеки имеют `expectContentType(JSON)` для понятной диагностики при HTML-ответе от стенда.

### Модели данных

Все модели — **Java records**, без Lombok.

```java
public record LoginBodyModel(String username, String password) {}
```

PATCH-модели (`UpdateUserModel`, `UpdateClubBodyModel`, `UpdateReviewBodyModel`) используют `@JsonInclude(NON_NULL)` — `null`-поля не сериализуются.

### Тестовые данные

- **`Fakers.FAKER`** — общий `Faker` инстанс (`net.datafaker`) для динамических данных.
- **`Fakers.shortUid()`** — 8-символьный UUID-suffix для уникальности.
- **`UserFixtures.createAndLogin(api)`** — регистрирует юзера через API и логинится, возвращает `TestUser(uid, username, password, token)`.
- **`ClubFixtures.sampleClub()`** — `CreateClubBodyModel` с faker-данными.
- **`ReviewFixtures.sampleReview(clubId)`** — `CreateReviewBodyModel` с faker-данными.

Граничные тестовые значения (`assessment=6/0`, `clubId=999_999_999`, `"u".repeat(256)`) — захардкожены намеренно: это часть тест-кейсов, а не данные.

### UI (Selenide)

- **`UiTestBase`** — конфиг Selenide (baseUrl, browser, headless), AllureSelenide listener, поддержка `selenide.remote` (Selenoid) с `enableVNC`/`enableVideo`. Наследник `TestBase` — даёт UI-тестам доступ к `api` для подготовки данных через API.
- **`BrowserAuth.loginViaApiAndOpenHome(api)`** — регистрирует юзера через API, логинится, открывает `/` и кладёт auth-state в `localStorage["book_club_auth"]` (точная структура подтверждена эмпирически). Возвращает `AuthSession`.
- **Page Objects** — `SignInPage`, `SignUpPage`, `ClubsListPage`, `ClubDetailPage`, `ReviewFormPage`. Селекторы — `data-testid` где есть, иначе CSS-классы Vue.

### Валидация ответов

- JSON Schema валидация через `json-schema-validator` в response-спеках.
- AssertJ для бизнес-проверок в тестах.
- Каждая проверка обёрнута в вложенный `Allure.step("...", () -> assertThat(...))` — в Allure-отчёте видно, какая именно проверка упала.

### Отчётность

Allure с кастомными FreeMarker-шаблонами (`resources/tpl/`) для запросов/ответов. Selenide-команды и скриншоты — через `AllureSelenide` listener.

## Структура проекта

```
src/test/
├── java/
│   ├── api/                # API-клиенты
│   ├── allure/             # CustomAllureListener
│   ├── models/             # Java records (login, logout, registration, users, clubs)
│   ├── specs/              # Request/Response спецификации
│   │   ├── BaseSpec
│   │   ├── login/, logout/, registration/, users/, clubs/
│   └── tests/
│       ├── Fakers, ClubFixtures, ReviewFixtures, UserFixtures, TestBase, TestData
│       ├── LoginTests, LogoutTests, RegistrationTests, UpdateUserTests
│       ├── ClubsTests             # GET /clubs/ (список)
│       ├── ClubsCrudTests         # POST/GET/PUT/PATCH/DELETE /clubs/{id}/
│       ├── ClubsPermissionsTests  # 403 на чужие клубы
│       ├── ClubMembersTests       # join/leave
│       ├── ClubReviewsCrudTests
│       ├── ClubReviewsPermissionsTests # 401/403/400
│       └── ui/
│           ├── UiTestBase, BrowserAuth
│           ├── pages/ (SignInPage, SignUpPage, ClubsListPage, ClubDetailPage, ReviewFormPage)
│           ├── UiAuthTests        # login/signup
│           ├── UiClubsListTests   # список/поиск/фильтры/пагинация
│           ├── UiClubDetailTests  # детальная страница + join
│           └── UiClubReviewsTests # форма отзыва + cancel + API-injection
└── resources/
    ├── schemas/            # JSON Schema по доменам
    ├── tpl/                # FreeMarker для Allure
    └── junit-platform.properties
```

## Зависимости

- **Тестирование:** JUnit 6, RestAssured 6, AssertJ
- **UI:** Selenide 7.16+
- **Валидация:** JSON Schema Validator
- **Тестовые данные:** Datafaker
- **Конфиги:** Owner (org.aeonbits.owner)
- **Отчётность:** Allure, allure-selenide
- **Утилиты:** Jackson
- **Форматирование:** Spotless с **palantir-java-format** (без wildcard-импортов)

## Запуск тестов

```bash
./gradlew test                                    # все тесты (параллельно по классам)
./gradlew test -Dgroups=API                       # только API (58 тестов, без браузера)
./gradlew test -Dgroups=UI                        # только UI (нужен Chrome/Selenoid)
./gradlew test -Dgroups=SMOKE                     # дымовые
./gradlew test -Dgroups=CLUBS,REGRESS             # OR-комбинация тегов
./gradlew test --tests 'LoginTests'               # один класс
./gradlew test --tests '*loginWithEmpty*'         # по паттерну
./gradlew spotlessApply                           # автоформатирование
./gradlew check                                   # spotlessCheck (только проверка)
./gradlew allureServe                             # Allure-отчёт на :19432
./gradlew clean test                              # чистый прогон
```

> **Внимание:** `spotlessApply` **не** запускается автоматически перед `test`. Если форматирование сбито — `./gradlew test` пройдёт, а `./gradlew check` упадёт. Запустите `./gradlew spotlessApply` вручную перед коммитом.

### Профили запуска (Owner)

Конфигурация UI и Selenide управляется через **Owner**-конфиг и properties-файлы. Профиль выбирается через `-Denv=...` (по умолчанию `local`).

```bash
./gradlew test -Dgroups=UI                      # env=local (по умолчанию)
./gradlew test -Dgroups=UI -Denv=remote         # Selenoid из remote.properties
./gradlew test -Dgroups=UI -Denv=remote -DbrowserVersion=132.0  # переопределить версию
```

Файлы конфигов:
- `src/test/resources/local.properties` — локальный запуск (Chrome через WebDriverManager, headless).
- `src/test/resources/remote.properties` — Selenoid (содержит `remoteUrl=https://user:pass@selenoid.host/wd/hub`).

Любое значение из properties можно переопределить через `-D<key>=<value>` (например `-Dbrowser=firefox`, `-DbrowserVersion=132.0`, `-DremoteUrl=...`).

При наличии `remoteUrl` Selenide использует remote-WebDriver, `headless` игнорируется (Selenoid сам управляет Chrome). VNC и видео включаются автоматически через `selenoid:options`.

Контракт WebConfig (`tests/config/WebConfig.java`):

| Ключ | Default | Описание |
|---|---|---|
| `browser` | `chrome` | Selenide browser |
| `browserVersion` | `128.0` | Версия (важна для Selenoid) |
| `browserSize` | `1920x1080` | Размер окна |
| `uiBaseUri` | `https://book-club.qa.guru` | Базовый URI UI |
| `remoteUrl` | — | URL Selenoid hub (если задан → remote режим) |

### Параллельный запуск

Тесты выполняются параллельно на уровне классов (`parallelism=4`). Методы внутри класса — последовательно. Конфиг: `src/test/resources/junit-platform.properties`.

Уникальность параллельных данных — `Fakers.shortUid()` + Datafaker. `UserFixtures.createAndLogin` гарантирует уникального юзера на каждый тест.

### Очистка тестовых данных

Тесты, создающие пользователей, обязаны удалять их в `@AfterEach` через `api.users.deleteCurrentUser(token)`. Backend каскадно удаляет связанные клубы и рецензии. Это держит стенд чистым.

Для тестов с двумя юзерами (`ClubMembersTests`, `ClubsPermissionsTests`, `ClubReviewsPermissionsTests`, `UiClubDetailTests.joinClub*`, `UiClubReviewsTests`) — `try/finally` чистит обоих с null-check.

## Теги (JUnit `@Tag`)

| Тег | Значение |
|---|---|
| `REGRESS` | Все тесты (полный регресс). |
| `API` | 58 тестов — все классы в `tests/`. |
| `UI` | 13 тестов — `tests/ui/*`. Требует браузер. |
| `SMOKE` | Позитивные happy-path в API и UI. |
| `LOGIN`, `LOGOUT`, `REGISTRATION`, `USER`, `CLUBS` | Domain-теги. |

## Конфигурация

| Параметр | Описание | По умолчанию |
|---|---|---|
| `groups` | Теги для запуска тестов (`-Dgroups=API,SMOKE`) | — |
| `api.baseUri` | Базовый URI API | `https://book-club.qa.guru` |
| `api.basePath` | Базовый путь API | `/api/v1` |
| `ui.baseUri` | Базовый URI UI | `https://book-club.qa.guru` |
| `env` | Профиль Owner-конфига (`local`/`remote`) | `local` |
| `browser` | Selenide browser | из `${env}.properties` |
| `browserVersion` | Версия браузера для Selenoid | из `${env}.properties` |
| `browserSize` | Размер окна | из `${env}.properties` |
| `headless` | Selenide headless (только local) | `true` |
| `remoteUrl` | URL Selenoid hub | из `remote.properties` |
| `allure.results.directory` | Директория результатов Allure | `build/allure-results` |

Все `api.*`, `ui.*`, `selenide.*`, `env`, `browser`, `browserVersion`, `headless`, `remoteUrl`, `groups` прокидываются в форкнутую JVM теста.

## Написание тестов

### Новый API-тест

```java
@Tag("API") @Tag("CLUBS") @Tag("REGRESS")
class NewFeatureTests extends TestBase {

    private String token;

    @BeforeEach
    void init() {
        token = UserFixtures.createAndLogin(api).token();
    }

    @AfterEach
    void cleanup() {
        api.users.deleteCurrentUser(token);
    }

    @Test @Tag("SMOKE")
    @Description("Описание теста")
    void testScenario() {
        ClubModel club = api.clubs.createClub(token, ClubFixtures.sampleClub());

        step("Проверки", () -> {
            step("id положительный", () -> assertThat(club.id()).isPositive());
            step("owner — текущий юзер", () -> assertThat(club.owner()).isPositive());
        });
    }
}
```

### Новый UI-тест

```java
@Tag("UI") @Tag("REGRESS")
class NewUiTests extends UiTestBase {

    private BrowserAuth.AuthSession session;

    @BeforeEach
    void authenticate() {
        session = BrowserAuth.loginViaApiAndOpenHome(api);
    }

    @AfterEach
    void cleanup() {
        if (session != null) {
            api.users.deleteCurrentUser(session.accessToken());
        }
    }

    @Test @Tag("SMOKE")
    @Description("Открытие клуба показывает заголовок")
    void clubDetailShowsTitle() {
        ClubModel club = api.clubs.createClub(session.accessToken(), ClubFixtures.sampleClub());

        new ClubDetailPage().openPage(club.id()).titleShouldBe(club.bookTitle());
    }
}
```

### Новая модель

```java
public record NewModel(String field1, Integer field2) {}
```

Для PATCH-моделей:
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatchModel(String field1, Integer field2) {}
```

### Новая спецификация

```java
public class NewSpec {
    public static final ResponseSpecification successResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectContentType(JSON)
            .expectBody(matchesJsonSchemaInClasspath("schemas/new/response_schema.json"))
            .build();
}
```

### Конвенции

- Модели — только records, без Lombok.
- Импорты — только явные, без `import foo.*` (Spotless блокирует билд).
- Все запросы к API — через клиенты, не `given()` напрямую.
- Авторизация — через `UserSpec.authRequestSpec(token)`, не ручной header.
- Каждый публичный метод ApiClient помечается `@Step("...")`.
- Каждый response-spec на 2xx с JSON-телом валидирует JSON Schema **и** `expectContentType(JSON)`; для 204/4xx без тела достаточно проверки статуса.
- Тестовые данные — через `Fakers.FAKER`/`ClubFixtures`/`ReviewFixtures`/`UserFixtures`. Граничные значения (`assessment=6`, `clubId=999_999_999`) — захардкожены.
- Тесты, создающие юзеров, обязаны удалять их в `@AfterEach`.
- Assertions оборачиваются во вложенный `Allure.step("Проверки", () -> step("...", () -> assertThat(...)))` для гранулярного отображения.
- UI-селекторы — приоритет `data-testid`, далее CSS-классы Vue.
- Все `static` поля в Spec-классах — `final`.
