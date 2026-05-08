package tests;

import api.ApiClient;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class TestBase {

    protected static final ApiClient api = new ApiClient();

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getProperty("api.baseUri", "https://book-club.qa.guru");
        RestAssured.basePath = System.getProperty("api.basePath", "/api/v1");
    }
}
