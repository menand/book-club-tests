package specs.logout;

import static io.restassured.filter.log.LogDetail.ALL;
import static specs.BaseSpec.baseRequestSpec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class LogoutSpec {

    public static RequestSpecification logoutRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulLogoutResponseSpec =
            new ResponseSpecBuilder().log(ALL).expectStatusCode(200).build();

    public static ResponseSpecification unauthorizedLogoutResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(401)
                    .expectBody("detail", org.hamcrest.Matchers.containsString("Token is invalid"))
                    .build();

    public static ResponseSpecification tokenBlacklistResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(401)
                    .expectBody(
                            "detail", org.hamcrest.Matchers.containsString("Token is blacklisted"))
                    .build();

    public static ResponseSpecification badRequestLogoutResponseSpec =
            new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(400)
                    .expectBody(
                            "refresh",
                            org.hamcrest.Matchers.hasItem(
                                    org.hamcrest.Matchers.containsString("may not")))
                    .build();
}
