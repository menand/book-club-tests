package tests.ui.pages;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class ClubsListPage {

    private final SelenideElement searchInput = $("input[placeholder*='Поиск']");
    private final ElementsCollection filterOptions = $$(".filter-option");
    private final ElementsCollection clubCards = $$(".club-card");
    private final SelenideElement paginationControls = $(".pagination-controls");
    private final ElementsCollection paginationButtons = $$(".pagination-button");

    public ClubsListPage openPage() {
        return step("Открыть список клубов /", () -> {
            open("/");
            return this;
        });
    }

    public ClubsListPage search(String query) {
        return step("Поиск клубов: " + query, () -> {
            searchInput.setValue(query).pressEnter();
            return this;
        });
    }

    public ClubsListPage setFilter(String label) {
        return step("Выбрать фильтр: " + label, () -> {
            filterOptions.findBy(text(label)).click();
            return this;
        });
    }

    public ClubsListPage clubsShouldBeVisible() {
        clubCards.shouldHave(sizeGreaterThanOrEqual(1));
        return this;
    }

    public ClubsListPage paginationShouldBeVisible() {
        paginationControls.shouldBe(visible);
        return this;
    }

    public ElementsCollection clubCards() {
        return clubCards;
    }

    public SelenideElement clubCardByTitle(String title) {
        return clubCards.findBy(text(title));
    }

    public ClubsListPage secondPageButtonShouldBeVisible() {
        paginationButtons
                .findBy(com.codeborne.selenide.Condition.exactText("2"))
                .shouldBe(visible);
        return this;
    }

    public ClubsListPage clickNextPage() {
        return step("Перейти на следующую страницу пагинации", () -> {
            paginationButtons.findBy(text("Вперед")).click();
            return this;
        });
    }
}
