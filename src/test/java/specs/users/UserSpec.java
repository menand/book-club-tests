package specs.users;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class UserSpec {

    public static RequestSpecification authRequestSpec(String token) {
        return RestAssured.given(baseRequestSpec).header("Authorization", "Bearer " + token);
    }

    public static ResponseSpecification successfulUserResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(
                            matchesJsonSchemaInClasspath("schemas/users/user_response_schema.json"))
                    .expectBody("id", notNullValue())
                    .expectBody("username", notNullValue())
                    .build();

    public static ResponseSpecification unauthorizedResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(401).build();
}
