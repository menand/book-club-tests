package allure;

import io.qameta.allure.restassured.AllureRestAssured;

public class CustomAllureListener {
    public static AllureRestAssured withCustomTemplate() {
        AllureRestAssured filter = new AllureRestAssured();
        filter.setRequestTemplate("request.ftl");
        filter.setResponseTemplate("response.ftl");
        return filter;
    }
}
