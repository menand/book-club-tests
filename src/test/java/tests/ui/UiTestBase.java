package tests.ui;

import static com.codeborne.selenide.Selenide.closeWebDriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import tests.TestBase;

public abstract class UiTestBase extends TestBase {

    @BeforeAll
    static void setUpSelenide() {
        Configuration.baseUrl = System.getProperty("ui.baseUri", "https://book-club.qa.guru");
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 6000;

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }
}
