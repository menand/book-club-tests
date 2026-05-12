package specs.users;

import static io.restassured.RestAssured.given;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class UserSpec {

    public static RequestSpecification authRequestSpec(String token) {
        return given(baseRequestSpec).header("Authorization", "Bearer " + token);
    }

    public static final ResponseSpecification successfulUserResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/users/user_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("username", notNullValue())
            .build();

    public static final ResponseSpecification unauthorizedResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(401).build();

    public static final ResponseSpecification userNoContentResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(204).build();
}
