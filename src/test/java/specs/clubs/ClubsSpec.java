package specs.clubs;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

public class ClubsSpec {

    public static final ResponseSpecification successfulClubsListResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/clubs/clubs_list_response_schema.json"))
            .expectBody("count", notNullValue())
            .expectBody("count", greaterThanOrEqualTo(0))
            .expectBody("results", notNullValue())
            .build();

    public static final ResponseSpecification successfulClubResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/clubs/club_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("bookTitle", notNullValue())
            .build();

    public static final ResponseSpecification createdClubResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath("schemas/clubs/club_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("bookTitle", notNullValue())
            .build();

    public static final ResponseSpecification clubNoContentResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(204).build();

    public static final ResponseSpecification clubNotFoundResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(404).build();

    public static final ResponseSpecification clubUnauthorizedResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(401).build();

    public static final ResponseSpecification clubForbiddenResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(403).build();

    public static final ResponseSpecification clubBadRequestResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(400).build();

    public static final ResponseSpecification successfulReviewResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/clubs/review_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("club", notNullValue())
            .build();

    public static final ResponseSpecification createdReviewResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath("schemas/clubs/review_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("club", notNullValue())
            .build();

    public static final ResponseSpecification successfulReviewsListResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/clubs/reviews_list_response_schema.json"))
            .expectBody("count", notNullValue())
            .expectBody("results", notNullValue())
            .build();
}
