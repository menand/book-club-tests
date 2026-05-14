package tests.ui;

import static com.codeborne.selenide.Selenide.closeWebDriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import java.util.Map;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.remote.DesiredCapabilities;
import tests.TestBase;
import tests.config.WebConfig;

public abstract class UiTestBase extends TestBase {

    private static final WebConfig CONFIG = ConfigFactory.newInstance().create(WebConfig.class, System.getProperties());

    @BeforeAll
    static void setUpSelenide() {
        Configuration.baseUrl = CONFIG.uiBaseUri();
        Configuration.browser = CONFIG.browser();
        Configuration.browserSize = CONFIG.browserSize();
        Configuration.browserVersion = CONFIG.browserVersion();
        Configuration.headless = CONFIG.headless();
        Configuration.timeout = 6000;

        String remote = CONFIG.remoteUrl();
        if (remote != null && !remote.isEmpty()) {
            Configuration.remote = remote;

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableVideo", true));
            Configuration.browserCapabilities = capabilities;
        }
    }

    @BeforeEach
    void addListener() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterEach
    void closeBrowser() {
        SelenideLogger.removeListener("allure");
        closeWebDriver();
    }
}
