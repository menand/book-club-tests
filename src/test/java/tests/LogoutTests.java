package tests;

import io.qameta.allure.Description;
import models.login.LoginBodyModel;
import models.logout.LogoutBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("LOGOUT")
@Tag("REGRESS")
class LogoutTests extends TestBase {

    private LoginBodyModel credentials;

    @BeforeEach
    void initUser() {
        UserFixtures.TestUser user = UserFixtures.createAndLogin(api);
        credentials = new LoginBodyModel(user.username(), user.password());
    }

    @AfterEach
    void cleanupUser() {
        if (credentials == null) {
            return;
        }
        String token = api.auth.loginAndGetAccessToken(credentials);
        api.users.deleteCurrentUser(token);
    }

    @Test
    @Tag("SMOKE")
    @Description("Проверка успешного выхода из системы с валидным refresh token")
    void successfulLogoutTest() {
        api.auth.logout(new LogoutBodyModel(api.auth.loginAndGetRefreshToken(credentials)));
    }

    @Test
    @Description("Попытка выхода с невалидным refresh token (например, изменённый или случайный) —"
            + " ожидается 401 Unauthorized")
    void logoutWithInvalidRefreshTokenTest() {
        String refreshToken = api.auth.loginAndGetRefreshToken(credentials);

        api.auth.logoutWithInvalidToken(new LogoutBodyModel(refreshToken + "a"));
    }

    @Test
    @Description("Попытка выхода с пустым refresh token — ожидается 400 Bad Request")
    void logoutWithEmptyRefreshTokenTest() {
        api.auth.logoutWithBadRequest(new LogoutBodyModel(""));
    }

    @Test
    @Description(
            "Попытка выхода с уже использованным refresh token (повторный logout) — ожидается 401" + " Unauthorized")
    void logoutWithUsedRefreshTokenTest() {
        String refreshToken = api.auth.loginAndGetRefreshToken(credentials);

        LogoutBodyModel logoutBody = new LogoutBodyModel(refreshToken);
        api.auth.logout(logoutBody);
        api.auth.logoutWithBlacklistedToken(logoutBody);
    }

    @Test
    @Description("Попытка выхода без токена (null) — ожидается 400 Bad Request")
    void logoutWithNullRefreshTokenTest() {
        api.auth.logoutWithBadRequest(new LogoutBodyModel(null));
    }
}
