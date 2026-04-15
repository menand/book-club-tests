package tests;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static specs.BaseSpec.baseRequestSpec;
import static tests.TestData.*;

import io.qameta.allure.Description;
import models.registration.ExistingUserResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrationTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    void prepareTestData() {
        // оставляем генерацию данных в тесте, чтобы каждый запуск был с новыми пользователями
        username = "user_" + System.currentTimeMillis();
        password = "pass_" + System.currentTimeMillis();
    }

    @Test
    @Description("Проверка успешной регистрации нового пользователя")
    void successfulRegistrationTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel registrationResponse =
                api.users.register(registrationData);

        assertThat(registrationResponse.id()).isGreaterThan(0);
        assertThat(registrationResponse.username()).isEqualTo(username);
        assertThat(registrationResponse.firstName()).isEmpty();
        assertThat(registrationResponse.lastName()).isEmpty();
        assertThat(registrationResponse.email()).isEmpty();

        assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);
    }

    @Test
    @Description("Попытка регистрации уже существующего пользователя")
    void existingUserWrongRegistrationTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel firstRegistrationResponse =
                api.users.register(registrationData);

        assertThat(firstRegistrationResponse.username()).isEqualTo(username);

        ExistingUserResponseModel secondRegistrationResponse =
                api.users.registerExistingUser(registrationData);

        String actualError = secondRegistrationResponse.username().getFirst();
        assertThat(actualError).isEqualTo(REGISTRATION_EXISTING_USER_ERROR);
    }

    @Test
    @Description("Отправка POST-запроса с методом GET — ожидается 405 Method Not Allowed")
    void registrationWithGetMethodNotAllowedTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        given().spec(baseRequestSpec)
                .body(registrationData)
                .when()
                .get("/users/register/")
                .then()
                .statusCode(405)
                .header("Allow", containsString("POST"));
    }

    @Test
    @Description(
            "Отправка данных в неподдерживаемом формате (application/xml) — ожидается 415"
                    + " Unsupported Media Type")
    void registrationWithUnsupportedMediaTypeTest() {
        given().spec(baseRequestSpec)
                .contentType("application/xml")
                .body(
                        "<user><username>"
                                + username
                                + "</username><password>"
                                + password
                                + "</password></user>")
                .when()
                .post("/users/register/")
                .then()
                .statusCode(415);
    }

    @Test
    @Description(
            "Отправка данных без обязательных полей (username и password) — ожидается 400 Bad"
                    + " Request")
    void registrationWithMissingRequiredFieldsTest() {

        given().spec(baseRequestSpec)
                .body("{}")
                .when()
                .post("/users/register/")
                .then()
                .statusCode(400);
    }

    @Test
    @Description("Регистрация с пустым паролем — ожидается 400 и сообщение об" + " ошибке")
    void registrationWithEmptyPasswordTest() {
        RegistrationBodyModel invalidData = new RegistrationBodyModel(username, "");

        given().spec(baseRequestSpec)
                .body(invalidData)
                .when()
                .post("/users/register/")
                .then()
                .statusCode(400)
                .body("password[0]", containsString("This field may not be blank."));
    }

    @Test
    @Description("Регистрация с слишком длинным username (256 символов) — ожидается 400")
    void registrationWithTooLongUsernameTest() {
        String longUsername = "u".repeat(256);
        RegistrationBodyModel registrationData = new RegistrationBodyModel(longUsername, password);

        given().spec(baseRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .statusCode(400)
                .body(
                        "username[0]",
                        containsString("Ensure this field has no more than 150 characters"));
    }
}
