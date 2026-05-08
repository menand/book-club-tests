package tests;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.TestData.REGISTRATION_EXISTING_USER_ERROR;
import static tests.TestData.REGISTRATION_IP_REGEXP;

import io.qameta.allure.Description;
import java.util.UUID;
import models.ValidationErrorResponseModel;
import models.login.LoginBodyModel;
import models.registration.ExistingUserResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("REGISTRATION")
@Tag("REGRESS")
class RegistrationTests extends TestBase {

    String username;
    String password;
    boolean userCreated;

    @BeforeEach
    void prepareTestData() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        username = "user_" + uid;
        password = "pass_" + uid;
        userCreated = false;
    }

    @AfterEach
    void cleanupUser() {
        if (userCreated) {
            String token = api.auth.loginAndGetAccessToken(new LoginBodyModel(username, password));
            api.users.deleteCurrentUser(token);
        }
    }

    @Test
    @Tag("SMOKE")
    @Description("Проверка успешной регистрации нового пользователя")
    void successfulRegistrationTest() {
        SuccessfulRegistrationResponseModel registrationResponse =
                api.users.register(new RegistrationBodyModel(username, password));
        userCreated = true;

        step("Проверки", () -> {
            step("id больше 0", () -> assertThat(registrationResponse.id()).isGreaterThan(0));
            step("username совпадает с переданным", () -> assertThat(registrationResponse.username())
                    .isEqualTo(username));
            step("firstName пустое", () -> assertThat(registrationResponse.firstName())
                    .isEmpty());
            step("lastName пустое", () -> assertThat(registrationResponse.lastName())
                    .isEmpty());
            step("email пустой", () -> assertThat(registrationResponse.email()).isEmpty());
            step("remoteAddr — валидный IP-адрес", () -> assertThat(registrationResponse.remoteAddr())
                    .matches(REGISTRATION_IP_REGEXP));
        });
    }

    @Test
    @Description("Попытка регистрации уже существующего пользователя")
    void existingUserWrongRegistrationTest() {
        RegistrationBodyModel regBody = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel firstResponse = api.users.register(regBody);
        userCreated = true;
        ExistingUserResponseModel secondResponse = api.users.registerExistingUser(regBody);

        step("Проверки", () -> {
            step("username первой регистрации совпадает с переданным", () -> assertThat(firstResponse.username())
                    .isEqualTo(username));
            step("первая ошибка username = 'A user with that username already exists.'",
                    () -> assertThat(secondResponse.username().getFirst()).isEqualTo(REGISTRATION_EXISTING_USER_ERROR));
        });
    }

    @Test
    @Description("Регистрация с пустым паролем — ожидается 400 и сообщение об ошибке")
    void registrationWithEmptyPasswordTest() {
        ValidationErrorResponseModel response =
                api.users.registerWithValidationError(new RegistrationBodyModel(username, ""));

        step("Проверки", () -> {
            step("ошибки по полю password присутствуют", () -> assertThat(response.password())
                    .isNotEmpty());
            step("первая ошибка password содержит 'This field may not be blank.'",
                    () -> assertThat(response.password().getFirst()).contains("This field may not be blank."));
        });
    }

    @Test
    @Description("Регистрация с слишком длинным username (256 символов) — ожидается 400")
    void registrationWithTooLongUsernameTest() {
        ValidationErrorResponseModel response =
                api.users.registerWithValidationError(new RegistrationBodyModel("u".repeat(256), password));

        step("Проверки", () -> {
            step("ошибки по полю username присутствуют", () -> assertThat(response.username())
                    .isNotEmpty());
            step("первая ошибка username — про лимит 150 символов", () -> assertThat(
                            response.username().getFirst())
                    .contains("Ensure this field has no more than 150" + " characters"));
        });
    }

    @Test
    @Description("Отправка POST-запроса с методом GET — ожидается 405 Method Not Allowed")
    void registrationWithGetMethodNotAllowedTest() {
        api.users.registerWithGetMethod(new RegistrationBodyModel(username, password));
    }

    @Test
    @Description(
            "Отправка данных в неподдерживаемом формате (application/xml) — ожидается 415" + " Unsupported Media Type")
    void registrationWithUnsupportedMediaTypeTest() {
        String xmlBody = "<user><username>" + username + "</username><password>" + password + "</password></user>";

        api.users.registerWithXmlContentType(xmlBody);
    }

    @Test
    @Description("Отправка данных без обязательных полей (username и password) — ожидается 400 Bad" + " Request")
    void registrationWithMissingRequiredFieldsTest() {
        api.users.registerWithEmptyBody();
    }
}
