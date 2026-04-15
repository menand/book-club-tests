package tests;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.baseRequestSpec;
import static specs.logout.LogoutSpec.*;
import static tests.TestData.LOGIN_PASSWORD;
import static tests.TestData.LOGIN_USERNAME;

import io.qameta.allure.Description;
import models.login.LoginBodyModel;
import models.logout.LogoutBodyModel;
import org.junit.jupiter.api.Test;

class LogoutTests extends TestBase {

    @Test
    @Description("Проверка успешного выхода из системы с валидным refresh token")
    void successfulLogoutTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD);
        String refreshToken = api.auth.loginAndGetRefreshToken(loginData);

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
        api.auth.logout(logoutData);
    }

    @Test
    @Description(
            "Попытка выхода с невалидным refresh token (например, изменённый или случайный) —"
                    + " ожидается 401 Unauthorized")
    void logoutWithInvalidRefreshTokenTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD);
        String refreshToken = api.auth.loginAndGetRefreshToken(loginData);

        // Модифицируем токен, чтобы сделать его невалидным
        String invalidRefreshToken = refreshToken + "a";

        LogoutBodyModel logoutData = new LogoutBodyModel(invalidRefreshToken);
        given().spec(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .log()
                .all()
                .spec(unauthorizedLogoutResponseSpec);
    }

    @Test
    @Description("Попытка выхода с пустым refresh token — ожидается 400 Bad Request")
    void logoutWithEmptyRefreshTokenTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel("");
        given().spec(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(badRequestLogoutResponseSpec);
    }

    @Test
    @Description(
            "Попытка выхода с уже использованным refresh token (повторный logout) — ожидается 401"
                    + " Unauthorized")
    void logoutWithUsedRefreshTokenTest() {
        LoginBodyModel loginData = new LoginBodyModel(LOGIN_USERNAME, LOGIN_PASSWORD);
        String refreshToken = api.auth.loginAndGetRefreshToken(loginData);

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
        api.auth.logout(logoutData);
        given().spec(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(tokenBlacklistResponseSpec);
    }

    @Test
    @Description("Попытка выхода без токена (null) — ожидается 400 Bad Request")
    void logoutWithNullRefreshTokenTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel(null);
        given().spec(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(badRequestLogoutResponseSpec);
    }
}
