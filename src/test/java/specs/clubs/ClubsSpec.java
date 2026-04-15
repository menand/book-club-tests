package specs.clubs;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class ClubsSpec {

    public static RequestSpecification clubsRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulClubsListResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/clubs/clubs_list_response_schema.json"))
                    .expectBody("count", notNullValue())
                    .expectBody("count", greaterThanOrEqualTo(0))
                    .expectBody("results", notNullValue())
                    .build();
}
