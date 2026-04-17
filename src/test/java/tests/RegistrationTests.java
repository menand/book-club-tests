package tests;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static specs.BaseSpec.baseRequestSpec;
import static specs.registration.RegistrationSpec.badRequestResponseSpec;
import static specs.registration.RegistrationSpec.methodNotAllowedResponseSpec;
import static specs.registration.RegistrationSpec.unsupportedMediaTypeResponseSpec;
import static tests.TestData.REGISTRATION_EXISTING_USER_ERROR;
import static tests.TestData.REGISTRATION_IP_REGEXP;

import io.qameta.allure.Description;
import java.util.UUID;
import models.ValidationErrorResponseModel;
import models.registration.ExistingUserResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("REGISTRATION")
@Tag("REGRESS")
class RegistrationTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    void prepareTestData() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        username = "user_" + uid;
        password = "pass_" + uid;
    }

    @Test
    @Tag("SMOKE")
    @Description("Проверка успешной регистрации нового пользователя")
    void successfulRegistrationTest() {
        SuccessfulRegistrationResponseModel registrationResponse =
                api.users.register(new RegistrationBodyModel(username, password));

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
        RegistrationBodyModel regBody = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel firstResponse = api.users.register(regBody);
        assertThat(firstResponse.username()).isEqualTo(username);

        ExistingUserResponseModel secondResponse = api.users.registerExistingUser(regBody);
        assertThat(secondResponse.username().getFirst())
                .isEqualTo(REGISTRATION_EXISTING_USER_ERROR);
    }

    @Test
    @Description("Регистрация с пустым паролем — ожидается 400 и сообщение об ошибке")
    void registrationWithEmptyPasswordTest() {
        ValidationErrorResponseModel response =
                api.users.registerWithValidationError(new RegistrationBodyModel(username, ""));

        assertThat(response.password()).isNotEmpty();
        assertThat(response.password().getFirst()).contains("This field may not be blank.");
    }

    @Test
    @Description("Регистрация с слишком длинным username (256 символов) — ожидается 400")
    void registrationWithTooLongUsernameTest() {
        ValidationErrorResponseModel response =
                api.users.registerWithValidationError(
                        new RegistrationBodyModel("u".repeat(256), password));

        assertThat(response.username()).isNotEmpty();
        assertThat(response.username().getFirst())
                .contains("Ensure this field has no more than 150 characters");
    }

    @Test
    @Description("Отправка POST-запроса с методом GET — ожидается 405 Method Not Allowed")
    void registrationWithGetMethodNotAllowedTest() {
        step("GET-запрос к /users/register/ — ожидается 405",
                () -> {
                    given(baseRequestSpec)
                            .body(new RegistrationBodyModel(username, password))
                            .when()
                            .get("/users/register/")
                            .then()
                            .spec(methodNotAllowedResponseSpec)
                            .header("Allow", containsString("POST"));
                });
    }

    @Test
    @Description(
            "Отправка данных в неподдерживаемом формате (application/xml) — ожидается 415"
                    + " Unsupported Media Type")
    void registrationWithUnsupportedMediaTypeTest() {
        step("POST XML в /users/register/ — ожидается 415",
                () -> {
                    given(baseRequestSpec)
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
                            .spec(unsupportedMediaTypeResponseSpec);
                });
    }

    @Test
    @Description(
            "Отправка данных без обязательных полей (username и password) — ожидается 400 Bad"
                    + " Request")
    void registrationWithMissingRequiredFieldsTest() {
        step("POST {} в /users/register/ — ожидается 400",
                () -> {
                    given(baseRequestSpec)
                            .body("{}")
                            .when()
                            .post("/users/register/")
                            .then()
                            .spec(badRequestResponseSpec);
                });
    }
}
