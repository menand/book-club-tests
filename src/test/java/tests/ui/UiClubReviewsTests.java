package tests.ui;

import static com.codeborne.selenide.Selenide.refresh;
import static tests.Fakers.FAKER;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.CreateReviewBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.ClubFixtures;
import tests.UserFixtures;
import tests.ui.pages.ClubDetailPage;

@Tag("UI")
@Tag("REGRESS")
class UiClubReviewsTests extends UiTestBase {

    private BrowserAuth.AuthSession session;
    private String ownerToken;
    private ClubModel club;

    @BeforeEach
    void initOwnerClubAndMember() {
        UserFixtures.TestUser owner = UserFixtures.createAndLogin(api);
        ownerToken = owner.token();
        club = api.clubs.createClub(ownerToken, ClubFixtures.sampleClub());

        session = BrowserAuth.loginViaApiAndOpenHome(api);
        api.clubs.joinClub(session.accessToken(), club.id());
    }

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
    @Description("Через UI: открыть свой клуб → 'Написать отзыв' → форма → отзыв в списке")
    void addReviewViaUiForm_displaysReviewInList() {
        String reviewText = FAKER.lorem().sentence(3);
        int assessment = FAKER.number().numberBetween(1, 5);
        int readPages = FAKER.number().numberBetween(50, 500);

        ClubDetailPage page = new ClubDetailPage().openPage(club.id()).noReviewsShouldBeVisible();

        page.clickAddReview().fill(assessment, readPages, reviewText).submit();

        page.reviewWithTextShouldBeVisible(reviewText);
    }

    @Test
    @Description("Клик 'Отмена' в форме отзыва закрывает форму и не создаёт отзыв")
    void cancelReviewForm_doesNotCreateReview() {
        ClubDetailPage page = new ClubDetailPage().openPage(club.id()).noReviewsShouldBeVisible();

        page.clickAddReview()
                .fill(
                        FAKER.number().numberBetween(1, 5),
                        FAKER.number().numberBetween(50, 500),
                        FAKER.lorem().sentence(3))
                .cancel();

        page.noReviewsShouldBeVisible().addReviewButtonShouldBeVisible();
    }

    @Test
    @Description("API-оптимизация: клуб без отзывов → создаём отзыв через API → refresh → отзыв виден")
    void apiReviewAppearsAfterRefresh() {
        String reviewText = FAKER.lorem().sentence(10);

        ClubDetailPage page = new ClubDetailPage().openPage(club.id()).noReviewsShouldBeVisible();

        api.clubs.createReview(
                session.accessToken(),
                new CreateReviewBodyModel(
                        club.id(),
                        reviewText,
                        FAKER.number().numberBetween(1, 5),
                        FAKER.number().numberBetween(50, 500)));

        refresh();

        page.reviewWithTextShouldBeVisible(reviewText);
    }
}
