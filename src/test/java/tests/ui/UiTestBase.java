package tests.ui;

import static com.codeborne.selenide.Selenide.closeWebDriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.remote.DesiredCapabilities;
import tests.TestBase;

public abstract class UiTestBase extends TestBase {

    @BeforeAll
    static void setUpSelenide() {
        Configuration.baseUrl = System.getProperty("ui.baseUri", "https://book-club.qa.guru");
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserSize = System.getProperty("browserSize", "1920x1080");
        Configuration.timeout = 6000;

        String remote = System.getProperty("selenide.remote");
        if (remote != null && !remote.isEmpty()) {
            Configuration.remote = remote;
            Configuration.browserVersion = System.getProperty("browserVersion", "128.0");

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableVideo", true));
            Configuration.browserCapabilities = capabilities;
        } else {
            Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        }

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }
}
