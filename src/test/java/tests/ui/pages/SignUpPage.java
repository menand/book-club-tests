package tests.ui.pages;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.SelenideElement;

public class SignUpPage {

    private final SelenideElement usernameInput = $("[data-testid=username-input]");
    private final SelenideElement passwordInput = $("[data-testid=password-input]");
    private final SelenideElement confirmPasswordInput = $("[data-testid=confirm-password-input]");
    private final SelenideElement signupButton = $("[data-testid=signup-button]");

    public SignUpPage openPage() {
        return step("Открыть /signup", () -> {
            open("/signup");
            return this;
        });
    }

    public SignUpPage register(String username, String password) {
        return step("Зарегистрироваться как " + username, () -> {
            usernameInput.setValue(username);
            passwordInput.setValue(password);
            confirmPasswordInput.setValue(password);
            signupButton.click();
            return this;
        });
    }
}
