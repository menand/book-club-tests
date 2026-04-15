package specs.login;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class LoginSpec {
    public static RequestSpecification loginRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulLoginResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/login/successful_login_response_schema.json"))
                    .expectBody("access", notNullValue())
                    .expectBody("refresh", notNullValue())
                    .build();

    public static ResponseSpecification wrongCredentialsLoginResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(401)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/login/wrong_credentials_login_response_schema.json"))
                    .expectBody("detail", notNullValue())
                    .build();

    public static ResponseSpecification validationErrorResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(400).build();

    public static ResponseSpecification methodNotAllowResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(405)
                    .expectBody("detail", notNullValue())
                    .build();
}
