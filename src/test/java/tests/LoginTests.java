package tests;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.TestData.LOGIN_PASSWORD;
import static tests.TestData.LOGIN_TOKEN_PREFIX;
import static tests.TestData.LOGIN_USERNAME;
import static tests.TestData.LOGIN_WRONG_CREDENTIALS_ERROR;
import static tests.TestData.LOGIN_WRONG_PASSWORD;

import io.qameta.allure.Description;
import models.ValidationErrorResponseModel;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.WrongCredentialsLoginResponseModel;
import org.junit.jupiter.api.Test;

class LoginTests extends TestBase {

    @Test
    @Description("Успешный вход в систему с валидными учетными данными")
    void successfulLoginTest() {
        SuccessfulLoginResponseModel loginResponse =
                api.auth.login(new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD));

        assertThat(loginResponse.access()).startsWith(LOGIN_TOKEN_PREFIX);
        assertThat(loginResponse.refresh()).startsWith(LOGIN_TOKEN_PREFIX);
        assertThat(loginResponse.access()).isNotEqualTo(loginResponse.refresh());
    }

    @Test
    @Description("Вход в систему с неверными учетными данными")
    void wrongCredentialsLoginTest() {
        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(
                        new LoginBodyModel(LOGIN_USERNAME, LOGIN_WRONG_PASSWORD));

        assertThat(loginResponse.detail()).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с пустым именем пользователя")
    void loginWithEmptyUsernameTest() {
        ValidationErrorResponseModel loginResponse =
                api.auth.loginWithValidationError(new LoginBodyModel("", LOGIN_PASSWORD));

        assertThat(loginResponse.username()).isNotEmpty();
        assertThat(loginResponse.username().getFirst()).contains("may not be blank");
    }

    @Test
    @Description("Вход в систему с пустым паролем")
    void loginWithEmptyPasswordTest() {
        ValidationErrorResponseModel loginResponse =
                api.auth.loginWithValidationError(new LoginBodyModel(LOGIN_USERNAME, ""));

        assertThat(loginResponse.password()).isNotEmpty();
        assertThat(loginResponse.password().getFirst()).contains("may not be blank");
    }

    @Test
    @Description("Вход в систему с null значением имени пользователя")
    void loginWithNullUsernameTest() {
        ValidationErrorResponseModel loginResponse =
                api.auth.loginWithValidationError(new LoginBodyModel(null, LOGIN_PASSWORD));

        assertThat(loginResponse.username()).isNotEmpty();
        assertThat(loginResponse.username().getFirst()).contains("may not be null");
    }

    @Test
    @Description("Вход в систему с null значением пароля")
    void loginWithNullPasswordTest() {
        ValidationErrorResponseModel loginResponse =
                api.auth.loginWithValidationError(new LoginBodyModel(LOGIN_USERNAME, null));

        assertThat(loginResponse.password()).isNotEmpty();
        assertThat(loginResponse.password().getFirst()).contains("may not be null");
    }

    @Test
    @Description("Вход в систему с пустыми учетными данными (имя пользователя и пароль)")
    void loginWithEmptyCredentialsTest() {
        ValidationErrorResponseModel loginResponse =
                api.auth.loginWithValidationError(new LoginBodyModel("", ""));

        assertThat(loginResponse.username()).isNotEmpty();
        assertThat(loginResponse.username().getFirst()).contains("may not be blank");
        assertThat(loginResponse.password()).isNotEmpty();
        assertThat(loginResponse.password().getFirst()).contains("may not be blank");
    }

    @Test
    @Description("Вход в систему с очень длинным именем пользователя")
    void loginWithVeryLongUsernameTest() {
        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(
                        new LoginBodyModel("x".repeat(1000), LOGIN_PASSWORD));

        assertThat(loginResponse.detail()).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с очень длинным паролем")
    void loginWithVeryLongPasswordTest() {
        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(
                        new LoginBodyModel(LOGIN_USERNAME, "x".repeat(1000)));

        assertThat(loginResponse.detail()).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с особыми символами в имени пользователя")
    void loginWithSpecialCharactersInUsernameTest() {
        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(
                        new LoginBodyModel("user!@#$%^&*()", LOGIN_PASSWORD));

        assertThat(loginResponse.detail()).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с особыми символами в пароле")
    void loginWithSpecialCharactersInPasswordTest() {
        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(
                        new LoginBodyModel(LOGIN_USERNAME, "pass!@#$%^&*()"));

        assertThat(loginResponse.detail()).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Попытка входа в систему с использованием метода DELETE вместо POST")
    void loginWithDeleteMethodTest() {
        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWithDeleteMethod(new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD));

        assertThat(loginResponse.detail()).contains("Method \"DELETE\" not allowed.");
    }
}
