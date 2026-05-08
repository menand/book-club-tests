# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

API-тесты для сервиса Book Club (`https://book-club.qa.guru`, базовый путь `/api/v1`). Java 21, JUnit 6, REST Assured 6, AssertJ, Allure, JSON Schema Validator. Все ассерты — AssertJ; модели — Java records.

## Команды

Всегда через Gradle wrapper:

```bash
./gradlew test                                    # все тесты, параллельно по классам
./gradlew test --tests LoginTests                 # один тестовый класс
./gradlew test --tests '*loginWithEmpty*'         # отдельный тест по паттерну
./gradlew test -Dgroups=SMOKE                     # по тегам JUnit
./gradlew test -Dgroups=LOGIN,REGRESS             # несколько тегов (OR)
./gradlew spotlessApply                           # автоформатирование (запускается и перед каждым test)
./gradlew spotlessCheck                           # проверка (запускается через ./gradlew check)
./gradlew allureServe                             # Allure-отчёт на :19432
./gradlew clean test                              # чистый прогон
```

Существующие теги: `LOGIN`, `LOGOUT`, `REGISTRATION`, `USER`, `CLUBS`, `SMOKE`, `REGRESS`. Добавляйте теги через `@Tag` на классе/методе.

`tasks.test` имеет `dependsOn(spotlessApply)` — форматирование применяется автоматически перед запуском тестов. Не нужно вызывать `spotlessApply` отдельно перед `test`.

## Архитектура

### Слои

```
tests/      ← extends TestBase, использует только api.*  (никакого given() в тестах)
api/        ← ApiClient → AuthApiClient / UsersApiClient / ClubsApiClient
specs/      ← Request- и Response-спеки (BaseSpec + домен)
models/     ← Java records (DTO для тел и ответов)
allure/     ← CustomAllureListener с FreeMarker-шаблонами
resources/schemas/  ← JSON Schema для валидации ответов
resources/tpl/      ← request.ftl / response.ftl для Allure
```

`ApiClient` — единая точка входа: `api.auth`, `api.users`, `api.clubs`. Каждый клиент инкапсулирует и позитивные, и негативные сценарии своего эндпоинта (см. `AuthApiClient.loginWrongCredentials`, `loginWithValidationError`, `logoutWithBlacklistedToken` и т.п.). Когда нужен новый сценарий — добавляйте метод в соответствующий ApiClient, не вызывайте `given()` из теста.

### Спеки

- `BaseSpec.baseRequestSpec` — фильтр Allure + `log().all()` + `ContentType.JSON`. Все request-спеки строятся от него.
- `UserSpec.authRequestSpec(token)` — добавляет `Authorization: Bearer <token>`. Никогда не выставляйте этот заголовок вручную в тестах/клиентах.
- Response-спеки валидируют статус-код И JSON Schema (`matchesJsonSchemaInClasspath(...)`). При добавлении нового эндпоинта обязательно создавайте схему в `resources/schemas/<домен>/`.

### Модели

- Только records, без Lombok (Lombok доступен, но новые модели — records).
- Accessors без префикса `get`: `response.access()`, `user.firstName()`.
- Для PATCH-запросов с опциональными полями используйте `@JsonInclude(NON_NULL)` (см. `UpdateUserModel`).
- Одноразовые DTO передавайте инлайн: `api.auth.login(new LoginBodyModel(user, pass))`.

### TestBase

Базовый URI и `basePath` выставляются в `TestBase.@BeforeAll`. `protected static final ApiClient api` — наследуется тестами. Все тестовые классы должны `extends TestBase`.

## Параллелизм

Конфиг в `src/test/resources/junit-platform.properties`: классы выполняются параллельно (`parallelism=4`), методы внутри класса — последовательно (`mode.default=same_thread`). `maxParallelForks` в Gradle = `availableProcessors / 2`.

Для уникальных данных (регистрация и т.п.) используйте `UUID.randomUUID()`, не `System.currentTimeMillis()` — иначе коллизии между параллельными классами.

## Spotless / стиль кода

- Google Java Format AOSP, `reflowLongStrings`, `skipJavadocFormatting`.
- **Wildcard-импорты запрещены** кастомным правилом — билд упадёт. Раскрывайте все `import foo.*;` в явные импорты, включая `static`.
- `removeUnusedImports`, `formatAnnotations`, `endWithNewline`.
- `ratchetFrom = 'origin/main'` — Spotless форматирует только изменённые относительно `main` файлы.
- Кастомное правило `Step one line` сжимает многострочный `step("...")` в одну строку.

## Конфигурация

Базовый URL **захардкожен** в `TestBase` (`https://book-club.qa.guru`). Если потребуется работа против другого окружения — менять там, а не в спеках.

Allure-результаты пишутся в `build/allure-results` (через `allure.results.directory` system property в `tasks.test`).

## Конвенции при изменениях

- Тест → ApiClient → Spec → Model → Schema. Добавление нового эндпоинта затрагивает все слои.
- Не вызывайте `given()` напрямую в тестах. Если этого требует новый сценарий — добавьте метод в ApiClient.
- Авторизация — через `UserSpec.authRequestSpec(token)`.
- Каждый публичный метод ApiClient помечайте `@Step("...")` для читаемых Allure-отчётов.
- AGENTS.md в корне устарел (упоминает Selenide / `ru.india.mall`) — это API-проект на REST Assured, package `tests`/`api`/`specs`/`models`. Не следуйте AGENTS.md дословно.

## MCP / документация

Для актуальных доков по библиотекам (REST Assured, JUnit 6, Allure, Jackson и т.п.) используйте `context7` MCP, а не веб-поиск.

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
