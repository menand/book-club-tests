package specs.logout;

import static io.restassured.filter.log.LogDetail.ALL;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

public class LogoutSpec {

    public static final ResponseSpecification successfulLogoutResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(200).build();

    public static final ResponseSpecification unauthorizedLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody("detail", containsString("Token is invalid"))
            .build();

    public static final ResponseSpecification tokenBlacklistResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody("detail", containsString("Token is blacklisted"))
            .build();

    public static final ResponseSpecification badRequestLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody("refresh", hasItem(containsString("may not")))
            .build();
}
