package tests.ui;

import static tests.Fakers.shortUid;

import io.qameta.allure.Description;
import models.login.LoginBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.UserFixtures;
import tests.ui.pages.ClubsListPage;
import tests.ui.pages.SignInPage;
import tests.ui.pages.SignUpPage;

@Tag("UI")
@Tag("REGRESS")
class UiAuthTests extends UiTestBase {

    private LoginBodyModel credentials;

    @AfterEach
    void cleanupUser() {
        if (credentials != null) {
            api.users.deleteCurrentUser(api.auth.loginAndGetAccessToken(credentials));
        }
    }

    @Test
    @Tag("SMOKE")
    @Description("Успешный логин через UI редиректит на список клубов")
    void successfulLoginRedirectsToClubs() {
        UserFixtures.TestUser user = UserFixtures.createAndLogin(api);
        credentials = new LoginBodyModel(user.username(), user.password());

        new SignInPage().openPage().loginAs(user.username(), user.password());

        new ClubsListPage().clubsShouldBeVisible();
    }

    @Test
    @Tag("SMOKE")
    @Description("Логин с неверным паролем показывает сообщение об ошибке")
    void wrongPasswordShowsError() {
        UserFixtures.TestUser user = UserFixtures.createAndLogin(api);
        credentials = new LoginBodyModel(user.username(), user.password());

        new SignInPage().openPage().loginAs(user.username(), "wrong-pass").errorShouldBe("Ты не пройдешь!");
    }

    @Test
    @Tag("SMOKE")
    @Description("Регистрация через UI создаёт юзера и логинит на список клубов")
    void signupCreatesUserAndLogsIn() {
        String uid = shortUid();
        String username = "user_" + uid;
        String password = "pass_" + uid;
        credentials = new LoginBodyModel(username, password);

        new SignUpPage().openPage().register(username, password);

        new ClubsListPage().clubsShouldBeVisible();
    }
}
