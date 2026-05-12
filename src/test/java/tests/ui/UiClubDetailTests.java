package tests.ui;

import static tests.Fakers.FAKER;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.ClubReviewModel;
import models.clubs.CreateReviewBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.ClubFixtures;
import tests.UserFixtures;
import tests.ui.pages.ClubDetailPage;

@Tag("UI")
@Tag("REGRESS")
class UiClubDetailTests extends UiTestBase {

    private BrowserAuth.AuthSession session;
    private String ownerToken;

    @AfterEach
    void cleanup() {
        try {
            if (session != null) {
                api.users.deleteCurrentUser(session.accessToken());
            }
        } finally {
            if (ownerToken != null) {
                api.users.deleteCurrentUser(ownerToken);
            }
        }
    }

    @Test
    @Tag("SMOKE")
    @Description("Открытие клуба по id показывает название и корректную ссылку Telegram")
    void openClubByIdShowsTitleAndTelegramLink() {
        session = BrowserAuth.loginViaApiAndOpenHome(api);
        ClubModel club = api.clubs.createClub(session.accessToken(), ClubFixtures.sampleClub());

        new ClubDetailPage()
                .openPage(club.id())
                .titleShouldBe(club.bookTitle())
                .telegramLinkShouldHaveHref(club.telegramChatLink());
    }

    @Test
    @Description("Детальная страница клуба содержит созданный отзыв")
    void clubWithReviewShowsItInList() {
        session = BrowserAuth.loginViaApiAndOpenHome(api);
        ClubModel club = api.clubs.createClub(session.accessToken(), ClubFixtures.sampleClub());

        String reviewText = FAKER.lorem().sentence(10);
        ClubReviewModel review = api.clubs.createReview(
                session.accessToken(),
                new CreateReviewBodyModel(
                        club.id(),
                        reviewText,
                        FAKER.number().numberBetween(1, 5),
                        FAKER.number().numberBetween(50, 500)));

        new ClubDetailPage().openPage(club.id()).reviewWithTextShouldBeVisible(review.review());
    }

    @Test
    @Tag("SMOKE")
    @Description("После клика 'Присоединиться' появляется кнопка 'Покинуть клуб'")
    void joinClubShowsLeaveButton() {
        UserFixtures.TestUser owner = UserFixtures.createAndLogin(api);
        ownerToken = owner.token();
        ClubModel club = api.clubs.createClub(ownerToken, ClubFixtures.sampleClub());

        session = BrowserAuth.loginViaApiAndOpenHome(api);

        new ClubDetailPage().openPage(club.id()).clickJoin().leaveButtonShouldBeVisible();
    }
}
