package tests.ui;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.refresh;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.ClubFixtures;
import tests.ui.pages.ClubsListPage;

@Tag("UI")
@Tag("REGRESS")
class UiClubsListTests extends UiTestBase {

    private BrowserAuth.AuthSession session;

    @BeforeEach
    void authenticate() {
        session = BrowserAuth.loginViaApiAndOpenHome(api);
    }

    @AfterEach
    void cleanupUser() {
        if (session != null) {
            api.users.deleteCurrentUser(session.accessToken());
        }
    }

    @Test
    @Tag("SMOKE")
    @Description("Открытие списка клубов показывает карточки и пагинацию")
    void clubsListLoadsWithCards() {
        new ClubsListPage().clubsShouldBeVisible().paginationShouldBeVisible();
    }

    @Test
    @Description("Поиск по названию книги фильтрует список до созданного клуба")
    void searchFiltersClubs() {
        ClubModel club = api.clubs.createClub(session.accessToken(), ClubFixtures.sampleClub());

        refresh();
        ClubsListPage page = new ClubsListPage().search(club.bookTitle());

        page.clubCardByTitle(club.bookTitle()).shouldBe(visible);
    }

    @Test
    @Description("Фильтр 'Мои клубы' показывает только клубы текущего юзера")
    void myClubsFilterShowsOnlyOwned() {
        ClubModel club = api.clubs.createClub(session.accessToken(), ClubFixtures.sampleClub());

        refresh();
        ClubsListPage page = new ClubsListPage().setFilter("Мои клубы");

        page.clubCards().shouldHave(size(1));
        page.clubCardByTitle(club.bookTitle()).shouldBe(visible);
    }

    @Test
    @Description("Клик 'Вперед' в пагинации переключает страницу — первая карточка меняется")
    void paginationNextOpensPage2() {
        ClubsListPage page = new ClubsListPage().clubsShouldBeVisible().paginationShouldBeVisible();
        page.secondPageButtonShouldBeVisible();

        String firstClubBefore = page.clubCards().first().getText();
        page.clickNextPage();

        page.clubCards().first().shouldNotHave(text(firstClubBefore));
    }
}
