# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Тест-фреймворк для сервиса Book Club. **API** (REST Assured 6 + JUnit 6 + AssertJ) против `https://book-club.qa.guru/api/v1` и **UI** (Selenide 7.16 + Vue 3 SPA) против `https://book-club.qa.guru/`. Allure, Datafaker, Spotless с palantir-java-format. Java 21.

## Команды

```bash
./gradlew test                                    # все 71 тест
./gradlew test -Dgroups=API                       # 58 API-тестов (без браузера)
./gradlew test -Dgroups=UI                        # 13 UI-тестов (нужен Chrome/Selenoid)
./gradlew test -Dgroups=SMOKE                     # дымовые
./gradlew test --tests 'ClubsCrudTests'           # один класс
./gradlew test --tests '*loginWithEmpty*'         # по паттерну
./gradlew spotlessApply                           # автоформатирование (вручную)
./gradlew check                                   # spotlessCheck без правки
./gradlew allureServe                             # Allure на :19432
```

Теги: `API`, `UI`, `REGRESS`, `SMOKE`, `LOGIN`, `LOGOUT`, `REGISTRATION`, `USER`, `CLUBS`.

`spotlessApply` **не** цеплён к `tasks.test` — `./gradlew test` не правит файлы. Вручную перед коммитом.

## Архитектура

### Слои

```
tests/                       ← API-тесты + fixtures
tests/ui/                    ← UI-тесты + Page Objects + BrowserAuth
api/                         ← ApiClient → AuthApiClient / UsersApiClient / ClubsApiClient
specs/                       ← Request- и Response-спеки
models/                      ← Java records
resources/schemas/           ← JSON Schema
resources/tpl/               ← request.ftl / response.ftl (Allure)
```

### API-слой

- `ApiClient` — единая точка входа: `api.auth`, `api.users`, `api.clubs`.
- Каждый клиент инкапсулирует и позитивные, и негативные сценарии (401/403/404/400/405/415). Тесты не вызывают `given()` напрямую.
- Все методы помечены `@Step("...")` для Allure.

### Spec-слой

- `BaseSpec.baseRequestSpec` — фильтр Allure + `log().all()` + `ContentType.JSON`. ApiClient'ы используют его напрямую (отдельных request-spec на домены нет).
- `UserSpec.authRequestSpec(token)` — `Authorization: Bearer`. Не выставлять header вручную.
- Response-спеки на 2xx с JSON-телом валидируют `expectContentType(JSON)` **до** `matchesJsonSchemaInClasspath(...)` — даёт понятную ошибку при HTML-ответе от стенда (502/504/debug-page).
- Все `static` поля в Spec — `final` (защита от мутации при параллельном прогоне).

### Модели

- Только records, без Lombok.
- Accessors без `get`: `response.access()`, `user.firstName()`.
- PATCH-модели: `@JsonInclude(NON_NULL)` (см. `UpdateUserModel`, `UpdateClubBodyModel`, `UpdateReviewBodyModel`).

### Fixtures (тестовые данные)

- `Fakers.FAKER` — общий `net.datafaker.Faker`.
- `Fakers.shortUid()` — 8-символьный UUID-суффикс.
- `UserFixtures.createAndLogin(api)` → `TestUser(uid, username, password, token)`. Регистрирует юзера через API и логинится.
- `ClubFixtures.sampleClub()` → `CreateClubBodyModel` (Faker book/author/sentence/year + UUID-уникальный bookTitle, потому что у бэка unique-constraint).
- `ReviewFixtures.sampleReview(clubId)` → `CreateReviewBodyModel`.
- Граничные значения (`assessment=6/0`, `clubId=999_999_999`, `"u".repeat(256)`, `"DELETE"` method) — **захардкожены**, это тест-кейсы, а не данные.

### UI (Selenide)

- `UiTestBase extends TestBase` — читает `WebConfig` (Owner) и настраивает Selenide. AllureSelenide listener, `closeWebDriver()` после каждого теста. Если задан `remoteUrl` → Selenoid с `enableVNC`/`enableVideo` и `browserVersion`.
- `BrowserAuth.loginViaApiAndOpenHome(api)` — регистрирует юзера через API, логинится, открывает `/`, кладёт точный JSON в `localStorage["book_club_auth"]` (структура подтверждена эмпирически), возвращает `AuthSession`. Подготовка UI-тестов идёт через API — UI логин используется только в `UiAuthTests`.
- Page Objects (`tests/ui/pages/`): `SignInPage`, `SignUpPage`, `ClubsListPage`, `ClubDetailPage`, `ReviewFormPage`. Селекторы — `data-testid` где есть, иначе CSS-классы Vue (`.club-card`, `.review-card`, `.add-review-btn`, `.pagination-button`, `.no-reviews`).

### Owner-конфиг (профили)

- `tests/config/WebConfig.java` — интерфейс Owner с ключами `browser`, `browserVersion`, `browserSize`, `uiBaseUri`, `remoteUrl`.
- `src/test/resources/${env}.properties` — `local.properties` (default) и `remote.properties` (с Selenoid `remoteUrl`).
- Выбор профиля: `-Denv=local` (default) или `-Denv=remote`.
- Переопределение: `-D<key>=<value>` бьёт значение в properties (например `-DbrowserVersion=132.0`).
- `env=local` прокидывается в JVM теста через `tasks.test { systemProperty "env", ... }` с дефолтом.

### TestBase

Базовый URI и `basePath` через `System.getProperty("api.baseUri", "https://book-club.qa.guru")`. `protected static final ApiClient api`. Все тестовые классы наследуют `TestBase` (или `UiTestBase`, который наследует `TestBase`).

## Параллелизм

`src/test/resources/junit-platform.properties`: классы concurrent (`parallelism=4`), методы внутри класса — последовательно. `maxParallelForks = availableProcessors / 2`.

Для уникальных данных — `Fakers.shortUid()` + Faker. `UserFixtures.createAndLogin` гарантирует уникального юзера на каждый тест.

## Очистка тестовых данных

Тесты, создающие юзеров, обязаны удалять их в `@AfterEach` через `api.users.deleteCurrentUser(token)`. Backend каскадно удаляет связанные клубы/рецензии — отдельный delete не нужен.

Для тестов с двумя юзерами (`ClubMembersTests`, `ClubsPermissionsTests`, `ClubReviewsPermissionsTests`, `UiClubDetailTests.joinClub*`, `UiClubReviewsTests`) — `try/finally` + null-check, чтобы второй juzer удалялся, даже если первый delete упал.

В `RegistrationTests` (где юзер создаётся в самом тесте, а не в `@BeforeEach`) — флаг `userCreated`, ставится после успешной регистрации; `@AfterEach` чистит только при true.

## Spotless / стиль кода

- **palantir-java-format** (с 2026-05-12 заменил Google Java Format AOSP — шире строка, лучше работает с AssertJ-цепочками).
- `removeUnusedImports`, `formatAnnotations`, `endWithNewline`.
- Кастомные правила: запрет wildcard-импортов (билд падает), `step("...")` сжимает в одну строку.
- `ratchetFrom = 'origin/main'` — Spotless форматирует только изменённое.

## Конфигурация (-D properties)

| Параметр | Назначение | Default |
|---|---|---|
| `groups` | Теги (`API`, `UI`, `SMOKE`, ...) | — |
| `env` | Owner-профиль (`local`/`remote`) | `local` |
| `api.baseUri` / `api.basePath` | API | `https://book-club.qa.guru` / `/api/v1` |
| `ui.baseUri` | UI (через WebConfig) | из `${env}.properties` |
| `browser` | Selenide | из `${env}.properties` |
| `browserVersion` | Selenoid | из `${env}.properties` |
| `browserSize` | Selenide | из `${env}.properties` |
| `headless` | Selenide local | `true` |
| `remoteUrl` | Selenoid hub URL | из `remote.properties` |

В `build.gradle` все `api.*`, `ui.*`, `selenide.*`, `env`, `browser`, `browserVersion`, `headless`, `remoteUrl` прокидываются в форкнутую JVM теста.

## Конвенции при изменениях

- **Архитектура слоёв**: Тест → ApiClient → Spec → Model → Schema.
- **Никаких `given()` в тестах** — добавьте метод в ApiClient.
- **Авторизация** — через `UserSpec.authRequestSpec(token)`.
- **Тестовые данные** — через `Fakers`/`ClubFixtures`/`ReviewFixtures`/`UserFixtures`. Граничные значения захардкожены.
- **Каждый ApiClient-метод** — `@Step("...")` для Allure.
- **Каждый 2xx-JSON-Spec** — `expectContentType(JSON)` + `matchesJsonSchemaInClasspath(...)`.
- **Assertions в тестах** — вложенный `Allure.step("Проверки", () -> step("...", () -> assertThat(...)))`, чтобы в Allure-дереве было видно, какая проверка упала.
- **Cleanup юзеров** в `@AfterEach`.
- **UI-селекторы** — приоритет `data-testid`, далее CSS-классы Vue.
- **`static` поля Spec** — всегда `final`.
- **`@Tag("API")`** на классах в `tests/`, **`@Tag("UI")`** — в `tests/ui/`. `@Tag("REGRESS")` — везде.

## Диагностика

- При `JsonParseException: Unexpected character ('<' ...)` — стенд вернул HTML вместо JSON (502/504/debug). Сейчас обрабатывается через `expectContentType(JSON)`: тест упадёт с понятным сообщением вместо Jackson-ошибки.
- При UI-падении на `Chrome instance exited` в Jenkins — забыт `-Dselenide.remote=...` или в `build.gradle` не прокидывается. Прокидка настроена для `selenide.*` и `ui.*` properties.

## MCP / документация

Для актуальных доков по библиотекам (REST Assured, Selenide, JUnit 6, Allure, Datafaker, Jackson) — `context7` MCP, не веб-поиск.
Для разведки UI/API стенда — `playwright` MCP (`browser_navigate`/`browser_evaluate`/`browser_snapshot`) + curl.

Версии библиотек проверять через `https://repo1.maven.org/maven2/<group>/<artifact>/maven-metadata.xml` (это сам репозиторий — всегда актуально). `search.maven.org/solrsearch` — индекс с задержкой, доверять нельзя.

---

## Поведенческие правила (общие)

**Bias toward caution over speed.** Для тривиальных задач — на ваше усмотрение.

### 1. Думать до кода
- Озвучивайте допущения. Сомневаетесь — спросите.
- Если есть несколько интерпретаций — представьте их, не выбирайте молча.
- Если есть проще решение — скажите. Возражайте, когда это оправдано.

### 2. Минимум кода
- Никаких фич сверх запрошенного.
- Никаких абстракций для одноразового кода.
- Никакой «гибкости» и «конфигурируемости», которую не просили.
- Никакой обработки невозможных сценариев.

### 3. Хирургические изменения
- Не «улучшайте» соседний код, комментарии и форматирование.
- Не рефакторите то, что не сломано.
- Подражайте существующему стилю, даже если сами писали бы иначе.
- Удаляйте только осиротевшие импорты/переменные/функции, ставшие ненужными из-за **ваших** правок.

### 4. Цель → проверка
- «Добавь валидацию» → «Напиши тесты на невалидные входы и зелени их».
- «Почини баг» → «Напиши тест, воспроизводящий баг, потом зелени».
- «Отрефактори X» → «Тесты зелёные до и после».
