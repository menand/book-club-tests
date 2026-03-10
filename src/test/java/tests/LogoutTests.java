package tests;

import models.login.LoginBodyModel;
import models.logout.LogoutBodyModel;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.logout.LogoutSpec.logoutRequestSpec;
import static specs.logout.LogoutSpec.successfulLogoutResponseSpec;
import static tests.TestData.LOGIN_USERNAME;
import static tests.TestData.LOGIN_PASSWORD;

public class LogoutTests extends TestBase {

    @Test
    public void successfulLogoutTest(){
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD);

        String refreshToken = step("Авторизация и получение токена", () ->
            given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract().path("refresh"));

        step("Отправка запроса logout с refresh-токеном и проверка ответа (200)", () -> {
            LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);

            given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfulLogoutResponseSpec);
        });
    }

    // todo add more negative tests
}
