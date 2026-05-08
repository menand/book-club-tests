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

    public static ResponseSpecification successfulClubResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(
                            matchesJsonSchemaInClasspath("schemas/clubs/club_response_schema.json"))
                    .expectBody("id", notNullValue())
                    .expectBody("bookTitle", notNullValue())
                    .build();

    public static ResponseSpecification createdClubResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(201)
                    .expectBody(
                            matchesJsonSchemaInClasspath("schemas/clubs/club_response_schema.json"))
                    .expectBody("id", notNullValue())
                    .expectBody("bookTitle", notNullValue())
                    .build();

    public static ResponseSpecification clubNoContentResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(204).build();

    public static ResponseSpecification clubNotFoundResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(404).build();

    public static ResponseSpecification clubUnauthorizedResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(401).build();

    public static ResponseSpecification successfulReviewResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/clubs/review_response_schema.json"))
                    .expectBody("id", notNullValue())
                    .expectBody("club", notNullValue())
                    .build();

    public static ResponseSpecification createdReviewResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(201)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/clubs/review_response_schema.json"))
                    .expectBody("id", notNullValue())
                    .expectBody("club", notNullValue())
                    .build();

    public static ResponseSpecification successfulReviewsListResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(
                            matchesJsonSchemaInClasspath(
                                    "schemas/clubs/reviews_list_response_schema.json"))
                    .expectBody("count", notNullValue())
                    .expectBody("results", notNullValue())
                    .build();
}
