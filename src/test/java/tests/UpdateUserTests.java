package tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
import java.util.UUID;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.users.UpdateUserModel;
import models.users.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateUserTests extends TestBase {

    private String token;
    private String testUsername;

    @BeforeEach
    void initUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        testUsername = "user_" + uid;
        String testPassword = "pass_" + uid;

        api.users.register(new RegistrationBodyModel(testUsername, testPassword));
        token = api.auth.loginAndGetAccessToken(new LoginBodyModel(testUsername, testPassword));
    }

    @Test
    @Description("Получение данных текущего пользователя")
    void getCurrentUserReturnsUserData() {
        UserModel user = api.users.getCurrentUser(token);

        assertThat(user.id()).isGreaterThan(0);
        assertThat(user.username()).isEqualTo(testUsername);
    }

    @Test
    @Description("Обновление пользователя через PUT")
    void updateUserWithPut() {
        UserModel updated =
                api.users.updateCurrentUser(
                        token,
                        new UpdateUserModel(testUsername, "Ivan", "Ivanov", "ivan@test.com"));

        assertThat(updated.firstName()).isEqualTo("Ivan");
        assertThat(updated.lastName()).isEqualTo("Ivanov");
        assertThat(updated.email()).isEqualTo("ivan@test.com");
    }

    @Test
    @Description("Частичное обновление пользователя через PATCH")
    void updateUserWithPatch() {
        UserModel updated =
                api.users.patchCurrentUser(
                        token, new UpdateUserModel(null, "Petr", "Petrov", null));

        assertThat(updated.firstName()).isEqualTo("Petr");
        assertThat(updated.lastName()).isEqualTo("Petrov");
    }

    @Test
    @Description("Доступ без токена должен вернуть 401")
    void unauthorizedAccessReturns401() {
        api.users.getCurrentUserUnauthorized();
    }
}
