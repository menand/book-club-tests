package tests;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
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
        testUsername = "user_" + System.currentTimeMillis();
        String testPassword = "pass_" + System.currentTimeMillis();

        RegistrationBodyModel regData = new RegistrationBodyModel(testUsername, testPassword);
        api.users.register(regData);

        LoginBodyModel loginData = new LoginBodyModel(testUsername, testPassword);
        token = api.auth.loginAndGetAccessToken(loginData);
    }

    @Test
    @Description("Получение данных текущего пользователя")
    void getCurrentUserReturnsUserData() {
        UserModel user = api.users.getCurrentUser(token);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUsername()).isEqualTo(testUsername);
    }

    @Test
    @Description("Обновление пользователя через PUT")
    void updateUserWithPut() {
        UpdateUserModel updateData =
                new UpdateUserModel(testUsername, "Ivan", "Ivanov", "ivan@test.com");

        UserModel updated = api.users.updateCurrentUser(token, updateData);

        assertThat(updated.getFirstName()).isEqualTo("Ivan");
        assertThat(updated.getLastName()).isEqualTo("Ivanov");
        assertThat(updated.getEmail()).isEqualTo("ivan@test.com");
    }

    @Test
    @Description("Частичное обновление пользователя через PATCH")
    void updateUserWithPatch() {
        UpdateUserModel patchData = new UpdateUserModel(null, "Petr", "Petrov", null);

        UserModel updated = api.users.patchCurrentUser(token, patchData);

        assertThat(updated.getFirstName()).isEqualTo("Petr");
        assertThat(updated.getLastName()).isEqualTo("Petrov");
    }

    @Test
    @Description("Доступ без токена должен вернуть 401")
    void unauthorizedAccessReturns401() {
        given().spec(specs.BaseSpec.baseRequestSpec)
                .when()
                .get("/users/me/")
                .then()
                .statusCode(401);
    }
}
