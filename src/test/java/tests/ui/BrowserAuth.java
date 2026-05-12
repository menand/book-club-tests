package tests.ui;

import api.ApiClient;
import com.codeborne.selenide.Selenide;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.users.UserModel;
import tests.UserFixtures;

class BrowserAuth {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    record AuthSession(UserFixtures.TestUser user, String accessToken, String refreshToken) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AuthStateUser(Integer id, String username, String firstName, String lastName, String email) {}

    private record AuthState(AuthStateUser user, String accessToken, String refreshToken, boolean isAuthenticated) {}

    static AuthSession loginViaApiAndOpenHome(ApiClient api) {
        return Allure.step("Подготовить юзера и авторизовать в браузере", () -> {
            UserFixtures.TestUser u = UserFixtures.createAndLogin(api);
            SuccessfulLoginResponseModel login = api.auth.login(new LoginBodyModel(u.username(), u.password()));
            UserModel me = api.users.getCurrentUser(login.access());

            String authJson = serializeAuthState(me, login);

            Selenide.open("/");
            Selenide.executeJavaScript("window.localStorage.setItem('book_club_auth', arguments[0]);", authJson);
            Selenide.refresh();

            return new AuthSession(u, login.access(), login.refresh());
        });
    }

    private static String serializeAuthState(UserModel me, SuccessfulLoginResponseModel login) {
        AuthStateUser user = new AuthStateUser(
                me.id(),
                me.username(),
                me.firstName() == null ? "" : me.firstName(),
                me.lastName() == null ? "" : me.lastName(),
                me.email() == null ? "" : me.email());
        AuthState state = new AuthState(user, login.access(), login.refresh(), true);
        try {
            return MAPPER.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать auth-state", e);
        }
    }
}
