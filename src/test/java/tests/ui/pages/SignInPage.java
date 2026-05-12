package tests.ui.pages;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.SelenideElement;

public class SignInPage {

    private final SelenideElement usernameInput = $("[data-testid=username-input]");
    private final SelenideElement passwordInput = $("[data-testid=password-input]");
    private final SelenideElement submitButton = $("[data-testid=submit-button]");
    private final SelenideElement errorMessage = $(".error");

    public SignInPage openPage() {
        return step("Открыть /signin", () -> {
            open("/signin");
            return this;
        });
    }

    public SignInPage loginAs(String username, String password) {
        return step("Войти как " + username, () -> {
            usernameInput.setValue(username);
            passwordInput.setValue(password);
            submitButton.click();
            return this;
        });
    }

    public SignInPage errorShouldBe(String expectedText) {
        errorMessage.shouldBe(visible).shouldHave(text(expectedText));
        return this;
    }
}
