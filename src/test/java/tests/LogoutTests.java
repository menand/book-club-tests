package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;

public class LogoutTests extends TestBase {

    String username = "qaguru";
    String password = "qaguru123";

    @Test
    public void successfulLogoutTest(){
        LoginBodyModel loginData = new LoginBodyModel(username, password);

        String refreshToken = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().path("refresh");

        // todo move to models & specs
        String logoutData = format("{\"refresh\": \"%s\"}", refreshToken);

        given()
            .log().all()
            .contentType(JSON)
            .body(logoutData)
            .basePath("/api/v1")
            .when()
            .post("/auth/logout/")
            .then()
            .log().all()
            .statusCode(200);

        // todo check logoutResponse is empty
    }

    // todo add more negative tests
}
