package tests.ui.pages;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class ClubDetailPage {

    private final SelenideElement title = $("h1");
    private final SelenideElement telegramLink = $("a[href*='t.me']");
    private final SelenideElement joinButton = $(".join-btn");
    private final SelenideElement leaveButton = $(".leave-btn");
    private final SelenideElement addReviewButton = $(".add-review-btn");
    private final SelenideElement noReviewsMessage = $(".no-reviews");
    private final ElementsCollection reviewCards = $$(".review-card");

    public ClubDetailPage openPage(int clubId) {
        return step("Открыть детальную страницу клуба id=" + clubId, () -> {
            open("/clubs/" + clubId);
            return this;
        });
    }

    public ClubDetailPage titleShouldBe(String expected) {
        title.shouldBe(visible).shouldHave(text(expected));
        return this;
    }

    public ClubDetailPage telegramLinkShouldHaveHref(String expectedHref) {
        telegramLink.shouldBe(visible).shouldHave(attribute("href", expectedHref));
        return this;
    }

    public ClubDetailPage reviewWithTextShouldBeVisible(String reviewText) {
        reviewCards.findBy(text(reviewText)).shouldBe(visible);
        return this;
    }

    public ClubDetailPage clickJoin() {
        return step("Присоединиться к клубу", () -> {
            joinButton.click();
            return this;
        });
    }

    public ClubDetailPage leaveButtonShouldBeVisible() {
        leaveButton.shouldBe(visible).shouldHave(text("Покинуть клуб"));
        return this;
    }

    public ClubDetailPage noReviewsShouldBeVisible() {
        noReviewsMessage.shouldBe(visible);
        return this;
    }

    public ReviewFormPage clickAddReview() {
        return step("Нажать 'Написать отзыв'", () -> {
            addReviewButton.click();
            return new ReviewFormPage();
        });
    }

    public ClubDetailPage addReviewButtonShouldBeVisible() {
        addReviewButton.shouldBe(visible);
        return this;
    }
}
