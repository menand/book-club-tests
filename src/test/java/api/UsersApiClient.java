package api;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.baseRequestSpec;
import static specs.registration.RegistrationSpec.badRequestResponseSpec;
import static specs.registration.RegistrationSpec.existingUserRegistrationResponseSpec;
import static specs.registration.RegistrationSpec.methodNotAllowedResponseSpec;
import static specs.registration.RegistrationSpec.registrationRequestSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;
import static specs.registration.RegistrationSpec.unsupportedMediaTypeResponseSpec;
import static specs.users.UserSpec.authRequestSpec;
import static specs.users.UserSpec.successfulUserResponseSpec;
import static specs.users.UserSpec.unauthorizedResponseSpec;
import static specs.users.UserSpec.userNoContentResponseSpec;

import io.qameta.allure.Step;
import models.ValidationErrorResponseModel;
import models.registration.ExistingUserResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.users.UpdateUserModel;
import models.users.UserModel;

public class UsersApiClient {

    @Step("Регистрация POST /users/register/")
    public SuccessfulRegistrationResponseModel register(RegistrationBodyModel body) {
        return given(registrationRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);
    }

    @Step("Регистрация уже существующего пользователя")
    public ExistingUserResponseModel registerExistingUser(RegistrationBodyModel body) {
        return given(registrationRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(existingUserRegistrationResponseSpec)
                .extract()
                .as(ExistingUserResponseModel.class);
    }

    @Step("Регистрация с ошибкой валидации")
    public ValidationErrorResponseModel registerWithValidationError(RegistrationBodyModel body) {
        return given(registrationRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestResponseSpec)
                .extract()
                .as(ValidationErrorResponseModel.class);
    }

    @Step("Получение данных текущего пользователя GET /users/me/")
    public UserModel getCurrentUser(String token) {
        return given(authRequestSpec(token))
                .when()
                .get("/users/me/")
                .then()
                .spec(successfulUserResponseSpec)
                .extract()
                .as(UserModel.class);
    }

    @Step("Обновление пользователя PUT /users/me/")
    public UserModel updateCurrentUser(String token, UpdateUserModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .put("/users/me/")
                .then()
                .spec(successfulUserResponseSpec)
                .extract()
                .as(UserModel.class);
    }

    @Step("Частичное обновление пользователя PATCH /users/me/")
    public UserModel patchCurrentUser(String token, UpdateUserModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successfulUserResponseSpec)
                .extract()
                .as(UserModel.class);
    }

    @Step("Получение данных пользователя без авторизации")
    public void getCurrentUserUnauthorized() {
        given(baseRequestSpec).when().get("/users/me/").then().spec(unauthorizedResponseSpec);
    }

    @Step("Удаление текущего пользователя DELETE /users/me/")
    public void deleteCurrentUser(String token) {
        given(authRequestSpec(token))
                .when()
                .delete("/users/me/")
                .then()
                .spec(userNoContentResponseSpec);
    }

    @Step("GET /users/register/ — ожидается 405 Method Not Allowed")
    public void registerWithGetMethod(RegistrationBodyModel body) {
        given(registrationRequestSpec)
                .body(body)
                .when()
                .get("/users/register/")
                .then()
                .spec(methodNotAllowedResponseSpec);
    }

    @Step("POST /users/register/ с XML — ожидается 415 Unsupported Media Type")
    public void registerWithXmlContentType(String xmlBody) {
        given(baseRequestSpec)
                .contentType("application/xml")
                .body(xmlBody)
                .when()
                .post("/users/register/")
                .then()
                .spec(unsupportedMediaTypeResponseSpec);
    }

    @Step("POST /users/register/ с пустым телом — ожидается 400 Bad Request")
    public void registerWithEmptyBody() {
        given(registrationRequestSpec)
                .body("{}")
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestResponseSpec);
    }
}
