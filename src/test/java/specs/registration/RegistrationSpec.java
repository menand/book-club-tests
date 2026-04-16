package specs.registration;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class RegistrationSpec {

    public static RequestSpecification registrationRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulRegistrationResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(201)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/registration/successful_registration_response_schema.json"))
                    .expectBody("id", notNullValue())
                    .expectBody("username", notNullValue())
                    .expectBody("remoteAddr", notNullValue())
                    .build();

    public static ResponseSpecification existingUserRegistrationResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(400)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/registration/existing_user_registration_response_schema.json"))
                    .expectBody("username", notNullValue())
                    .build();

    public static ResponseSpecification badRequestResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(400).build();

    public static ResponseSpecification methodNotAllowedResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(405).build();

    public static ResponseSpecification unsupportedMediaTypeResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(415).build();
}
