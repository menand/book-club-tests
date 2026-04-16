package api;

import static io.restassured.RestAssured.given;
import static specs.registration.RegistrationSpec.existingUserRegistrationResponseSpec;
import static specs.registration.RegistrationSpec.registrationRequestSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;

import models.registration.ExistingUserResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.users.UpdateUserModel;
import models.users.UserModel;

public class UsersApiClient {

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

    public UserModel getCurrentUser(String token) {
        return given().header("Authorization", "Bearer " + token)
                .relaxedHTTPSValidation()
                .when()
                .get("/users/me/")
                .then()
                .statusCode(200)
                .extract()
                .as(UserModel.class);
    }

    public UserModel updateCurrentUser(String token, UpdateUserModel body) {
        return given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .relaxedHTTPSValidation()
                .body(body)
                .when()
                .put("/users/me/")
                .then()
                .statusCode(200)
                .extract()
                .as(UserModel.class);
    }

    public UserModel patchCurrentUser(String token, UpdateUserModel body) {
        return given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .relaxedHTTPSValidation()
                .body(body)
                .when()
                .patch("/users/me/")
                .then()
                .statusCode(200)
                .extract()
                .as(UserModel.class);
    }
}
