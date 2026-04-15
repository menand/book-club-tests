package api;

import static io.restassured.RestAssured.given;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.methodNotAllowResponseSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.login.LoginSpec.validationErrorResponseSpec;
import static specs.login.LoginSpec.wrongCredentialsLoginResponseSpec;
import static specs.logout.LogoutSpec.logoutRequestSpec;
import static specs.logout.LogoutSpec.successfulLogoutResponseSpec;

import io.qameta.allure.Step;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.ValidationErrorResponseModel;
import models.login.WrongCredentialsLoginResponseModel;
import models.logout.LogoutBodyModel;

public class AuthApiClient {

    public SuccessfulLoginResponseModel login(LoginBodyModel loginBody) {
        return given(loginRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);
    }

    @Step("Авторизация и получение токена")
    public String loginAndGetRefreshToken(LoginBodyModel loginBody) {
        return given(loginRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .path("refresh");
    }

    public String loginAndGetAccessToken(LoginBodyModel loginBody) {
        return given(loginRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .path("access");
    }

    public WrongCredentialsLoginResponseModel loginWrongCredentials(LoginBodyModel loginBody) {
        return given(loginRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialsLoginResponseSpec)
                .extract()
                .as(WrongCredentialsLoginResponseModel.class);
    }

    public ValidationErrorResponseModel loginWithValidationError(LoginBodyModel loginBody) {
        return given(loginRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(validationErrorResponseSpec)
                .extract()
                .as(ValidationErrorResponseModel.class);
    }

    @Step("Отправка запроса logout")
    public void logout(LogoutBodyModel logoutBody) {
        given(logoutRequestSpec)
                .body(logoutBody)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfulLogoutResponseSpec);
    }

    @Step("Попытка входа в систему с использованием метода DELETE")
    public WrongCredentialsLoginResponseModel loginWithDeleteMethod(LoginBodyModel loginBody) {
        return given(loginRequestSpec)
                .body(loginBody)
                .when()
                .delete("/auth/token/")
                .then()
                .spec(methodNotAllowResponseSpec)
                .extract()
                .as(WrongCredentialsLoginResponseModel.class);
    }
}
