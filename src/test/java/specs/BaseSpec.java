package specs;

import static allure.CustomAllureListener.withCustomTemplate;
import static io.restassured.RestAssured.with;
import static io.restassured.http.ContentType.JSON;

import io.restassured.specification.RequestSpecification;

public class BaseSpec {

    public static RequestSpecification baseRequestSpec =
            with().filter(withCustomTemplate()).log().all().contentType(JSON);
}
