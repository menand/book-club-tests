package tests;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.TestData.*;

import io.qameta.allure.Description;
import java.util.List;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.ValidationErrorResponseModel;
import models.login.WrongCredentialsLoginResponseModel;
import org.junit.jupiter.api.Test;

class LoginTests extends TestBase {

    @Test
    @Description("Успешный вход в систему с валидными учетными данными")
    void successfulLoginTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD);

        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);

        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();
        assertThat(actualAccess).startsWith(LOGIN_TOKEN_PREFIX);
        assertThat(actualRefresh).startsWith(LOGIN_TOKEN_PREFIX);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);
    }

    @Test
    @Description("Вход в систему с неверными учетными данными")
    void wrongCredentialsLoginTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_WRONG_PASSWORD);

        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(loginData);

        String actualDetailError = loginResponse.detail();
        assertThat(actualDetailError).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с пустым именем пользователя")
    void loginWithEmptyUsernameTest() {
        LoginBodyModel loginData = new LoginBodyModel("", LOGIN_PASSWORD);

        ValidationErrorResponseModel loginResponse = api.auth.loginWithValidationError(loginData);

        List<String> usernameErrors = loginResponse.username();
        assertThat(usernameErrors).isNotEmpty();
        assertThat(usernameErrors.getFirst()).contains("may not be blank");
    }

    @Test
    @Description("Вход в систему с пустым паролем")
    void loginWithEmptyPasswordTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, "");

        ValidationErrorResponseModel loginResponse = api.auth.loginWithValidationError(loginData);

        assertThat(loginResponse.hasPasswordError()).isTrue();
        assertThat(loginResponse.getPasswordError()).contains("may not be blank");
    }

    @Test
    @Description("Вход в систему с null значением имени пользователя")
    void loginWithNullUsernameTest() {
        LoginBodyModel loginData = new LoginBodyModel(null, LOGIN_PASSWORD);

        ValidationErrorResponseModel loginResponse = api.auth.loginWithValidationError(loginData);

        assertThat(loginResponse.hasUsernameError()).isTrue();
        assertThat(loginResponse.getUsernameError()).contains("may not be null");
    }

    @Test
    @Description("Вход в систему с null значением пароля")
    void loginWithNullPasswordTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, null);

        ValidationErrorResponseModel loginResponse = api.auth.loginWithValidationError(loginData);

        assertThat(loginResponse.hasPasswordError()).isTrue();
        assertThat(loginResponse.getPasswordError()).contains("may not be null");
    }

    @Test
    @Description("Вход в систему с пустыми учетными данными (имя пользователя и пароль)")
    void loginWithEmptyCredentialsTest() {
        LoginBodyModel loginData = new LoginBodyModel("", "");

        ValidationErrorResponseModel loginResponse = api.auth.loginWithValidationError(loginData);

        List<String> usernameErrors = loginResponse.username();
        List<String> passwordErrors = loginResponse.password();
        assertThat(usernameErrors).isNotEmpty();
        assertThat(usernameErrors.getFirst()).contains("may not be blank");
        assertThat(passwordErrors).isNotEmpty();
        assertThat(passwordErrors.getFirst()).contains("may not be blank");
    }

    @Test
    @Description("Вход в систему с очень длинным именем пользователя")
    void loginWithVeryLongUsernameTest() {
        String longUsername = "x".repeat(1000);
        LoginBodyModel loginData = new LoginBodyModel(longUsername, LOGIN_PASSWORD);

        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(loginData);

        String actualDetailError = loginResponse.detail();
        assertThat(actualDetailError).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с очень длинным паролем")
    void loginWithVeryLongPasswordTest() {
        String longPassword = "x".repeat(1000);
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, longPassword);

        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(loginData);

        String actualDetailError = loginResponse.detail();
        assertThat(actualDetailError).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с особыми символами в имени пользователя")
    void loginWithSpecialCharactersInUsernameTest() {
        String specialUsername = "user!@#$%^&*()";
        LoginBodyModel loginData = new LoginBodyModel(specialUsername, LOGIN_PASSWORD);

        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(loginData);

        String actualDetailError = loginResponse.detail();
        assertThat(actualDetailError).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Вход в систему с особыми символами в пароле")
    void loginWithSpecialCharactersInPasswordTest() {
        String specialPassword = "pass!@#$%^&*()";
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, specialPassword);

        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWrongCredentials(loginData);

        String actualDetailError = loginResponse.detail();
        assertThat(actualDetailError).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @Description("Попытка входа в систему с использованием метода DELETE вместо POST")
    void loginWithDeleteMethodTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD);

        WrongCredentialsLoginResponseModel loginResponse =
                api.auth.loginWithDeleteMethod(loginData);

        String actualDetailError = loginResponse.detail();
        assertThat(actualDetailError).contains("Method \"DELETE\" not allowed.");
    }
}
