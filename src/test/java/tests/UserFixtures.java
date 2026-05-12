package tests;

import static tests.Fakers.shortUid;

import api.ApiClient;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;

public class UserFixtures {

    public record TestUser(String uid, String username, String password, String token) {}

    public static TestUser createAndLogin(ApiClient api) {
        String uid = shortUid();
        String username = "user_" + uid;
        String password = "pass_" + uid;

        api.users.register(new RegistrationBodyModel(username, password));
        String token = api.auth.loginAndGetAccessToken(new LoginBodyModel(username, password));

        return new TestUser(uid, username, password, token);
    }
}
