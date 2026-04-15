# Book Club API Test Automation Framework

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/)
[![JUnit](https://img.shields.io/badge/Test_Framework-JUnit_6-green.svg)](https://junit.org/junit5/)
[![RestAssured](https://img.shields.io/badge/API_Testing-RestAssured-orange.svg)](https://rest-assured.io/)
[![Allure](https://img.shields.io/badge/Reporting-Allure-purple.svg)](https://docs.qameta.io/allure/)

## Описание проекта

Этот проект представляет собой фреймворк для автоматизированного тестирования API сервиса Book Club. Проект реализован на Java с использованием современных подходов и лучших практик в области тестирования API.

Фреймворк предоставляет комплексное покрытие функциональности API, включая аутентификацию, регистрацию пользователей, управление профилями и работу с книжными клубами. Архитектура проекта следует принципам модульности и переиспользования кода.

## Архитектурные особенности

Проект построен с использованием следующих ключевых паттернов и подходов:

### 1. Page Object Model для API

Реализован через класс `ApiClient`, который служит единой точкой доступа ко всем API-клиентам:
- `AuthApiClient` - для операций аутентификации
- `UsersApiClient` - для управления пользователями
- `ClubsApiClient` - для работы с книжными клубами

### 2. Спецификации (Specs)

Используется паттерн спецификаций для переиспользования настроек запросов и валидации ответов:
- `BaseSpec` - базовая спецификация с общими настройками
- `LoginSpec`, `RegistrationSpec`, `UserSpec`, `ClubsSpec` - спецификации для конкретных эндпоинтов

### 3. Модели данных

Используются record-ы Java для представления моделей данных, что обеспечивает:
- Неизменяемость
- Безопасность потоков
- Минимальный boilerplate код
- Автоматическую реализацию equals(), hashCode(), toString()

### 4. Валидация ответов

- JSON Schema валидация через `json-schema-validator`
- Гибкие проверки с использованием Hamcrest и AssertJ
- Автоматическая валидация структуры ответов

### 5. Отчетность

- Интеграция с Allure для генерации детализированных отчетов
- Кастомные шаблоны для отображения запросов и ответов
- Поддержка аттачментов (запросы, ответы)

## Структура проекта

```
src/
├── test/
│   ├── java/
│   │   ├── api/                # API-клиенты
│   │   ├── models/            # Модели данных
│   │   ├── specs/             # Спецификации запросов/ответов
│   │   └── tests/             # Тесты
│   └── resources/
│       ├── schemas/          # JSON Schema для валидации
│       └── tpl/              # Шаблоны для Allure
```

## Зависимости

Проект использует следующие основные зависимости:

- **Тестирование:** JUnit 6, RestAssured, AssertJ
- **Логирование:** SLF4J, Log4j
- **Валидация:** JSON Schema Validator
- **Отчетность:** Allure
- **Утилиты:** Jackson
- **Форматирование:** Spotless (Google Java Format)

## Настройка окружения

### Предварительные требования

- Java 21 или выше
- Gradle 8.x
- Git

### Установка

1. Клонируйте репозиторий:
```bash
git clone https://github.com/menand/book-club-tests.git
cd book-club-tests
```

2. Установите зависимости:
```bash
./gradlew build
```

## Запуск тестов

### Запуск всех тестов
```bash
./gradlew test
```

### Запуск тестов с параметрами
```bash
# Запуск тестов с определенными тегами
./gradlew test -Dgroups=smoke,regression

# Запуск конкретного теста
./gradlew test --tests LoginTests
```

### Генерация отчетов Allure

После выполнения тестов сгенерируйте отчет Allure:

```bash
# Генерация отчета
./gradlew allureReport

# Запуск сервера Allure для просмотра отчета
./gradlew allureServe
```

Отчет будет доступен по адресу `http://localhost:19432`.

## Конфигурация

Конфигурационные параметры можно задавать через системные свойства:

| Параметр | Описание | Значение по умолчанию |
|---------|---------|---------------------|
| `groups` | Теги для запуска тестов | - |
| `allure.results.directory` | Директория для результатов Allure | build/allure-results |

## Написание тестов

### Создание новых тестов

1. Создайте новый класс тестов в пакете `tests`
2. Унаследуйтесь от `TestBase` для доступа к базовым настройкам
3. Используйте инъекцию `api` из `TestBase` для доступа к API-клиентам

```java
public class NewFeatureTests extends TestBase {

    @Test
    @Description("Описание теста")
    void testScenario() {
        // Используйте api.auth, api.users, api.clubs для вызова API
        SuccessfulLoginResponseModel response = api.auth.login(loginData);

        // Проверки с использованием AssertJ
        assertThat(response.access()).startsWith("eyJ");
    }
}
```

### Создание новых моделей

Используйте record для создания моделей данных:

```java
public record NewModel(String field1, Integer field2) {}
```

### Создание спецификаций

Создайте новую спецификацию в пакете `specs`:

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

## Лучшие практики

1. **Именование тестов:** Используйте описательные имена, отражающие суть теста
2. **Организация тестов:** Группируйте тесты по функциональности в отдельные классы
3. **Покрытие:** Обеспечьте покрытие позитивных, негативных и граничных сценариев
4. **Независимость:** Тесты должны быть независимыми и не зависеть от порядка выполнения
5. **Чистота:** Используйте `@BeforeEach` и `@AfterEach` для подготовки и очистки данных

## Поддержка и вклад

Если у вас есть предложения по улучшению проекта или вы нашли баг, пожалуйста, создайте issue в репозитории.

Для внесения изменений:

1. Создайте fork репозитория
2. Создайте новую ветку (`git checkout -b feature/new-feature`)
3. Внесите изменения
4. Сделайте коммит (`git commit -am 'Add new feature'`)
5. Запушьте изменения (`git push origin feature/new-feature`)
6. Создайте pull request
