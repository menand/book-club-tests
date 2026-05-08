package tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
import java.util.UUID;
import models.clubs.ClubModel;
import models.clubs.ClubReviewModel;
import models.clubs.CreateReviewBodyModel;
import models.clubs.ReviewsListResponseModel;
import models.clubs.UpdateReviewBodyModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.users.UserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubReviewsCrudTests extends TestBase {

    private String token;
    private Integer clubId;

    @BeforeEach
    void initUserAndClub() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + uid;
        String password = "pass_" + uid;

        api.users.register(new RegistrationBodyModel(username, password));
        token = api.auth.loginAndGetAccessToken(new LoginBodyModel(username, password));

        ClubModel club = api.clubs.createClub(token, ClubFixtures.sampleClub(uid));
        clubId = club.id();
    }

    @AfterEach
    void cleanupUser() {
        api.users.deleteCurrentUser(token);
    }

    @Test
    @Tag("SMOKE")
    @Description("Создание рецензии возвращает 201 и заполненный объект")
    void createReview_returnsCreatedReview() {
        ClubReviewModel created =
                api.clubs.createReview(
                        token, new CreateReviewBodyModel(clubId, "Excellent read", 5, 320));

        UserModel currentUser = api.users.getCurrentUser(token);

        assertThat(created.id()).isPositive();
        assertThat(created.club()).isEqualTo(clubId);
        assertThat(created.review()).isEqualTo("Excellent read");
        assertThat(created.assessment()).isEqualTo(5);
        assertThat(created.readPages()).isEqualTo(320);
        assertThat(created.user().id()).isEqualTo(currentUser.id());
        assertThat(created.user().username()).isEqualTo(currentUser.username());
    }

    @Test
    @Tag("SMOKE")
    @Description("Получение рецензии по id возвращает данные созданной")
    void getReviewById_returnsReviewData() {
        ClubReviewModel created =
                api.clubs.createReview(
                        token, new CreateReviewBodyModel(clubId, "Good book", 4, 200));

        ClubReviewModel fetched = api.clubs.getReview(token, created.id());

        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.review()).isEqualTo("Good book");
        assertThat(fetched.assessment()).isEqualTo(4);
    }

    @Test
    @Description("Список рецензий содержит созданную")
    void getReviewsList_includesCreated() {
        ClubReviewModel created =
                api.clubs.createReview(
                        token, new CreateReviewBodyModel(clubId, "Listed review", 3, 100));

        ReviewsListResponseModel list = api.clubs.getReviews(token);

        assertThat(list.results()).extracting(ClubReviewModel::id).contains(created.id());
    }

    @Test
    @Description("PUT рецензии полностью заменяет все поля")
    void updateReviewWithPut_replacesAllFields() {
        ClubReviewModel created =
                api.clubs.createReview(token, new CreateReviewBodyModel(clubId, "Initial", 3, 50));

        ClubReviewModel updated =
                api.clubs.updateReview(
                        token,
                        created.id(),
                        new CreateReviewBodyModel(clubId, "Replaced text", 5, 500));

        assertThat(updated.review()).isEqualTo("Replaced text");
        assertThat(updated.assessment()).isEqualTo(5);
        assertThat(updated.readPages()).isEqualTo(500);
    }

    @Test
    @Description("PATCH рецензии обновляет только переданные поля")
    void patchReviewWithPatch_updatesPartial() {
        ClubReviewModel created =
                api.clubs.createReview(token, new CreateReviewBodyModel(clubId, "Original", 2, 75));

        ClubReviewModel patched =
                api.clubs.patchReview(
                        token, created.id(), new UpdateReviewBodyModel(null, null, 4, null));

        assertThat(patched.assessment()).isEqualTo(4);
        assertThat(patched.review()).isEqualTo("Original");
        assertThat(patched.readPages()).isEqualTo(75);
    }

    @Test
    @Description("DELETE рецензии возвращает 204; последующий GET — 404")
    void deleteReview_returns204_andSubsequentGetReturns404() {
        ClubReviewModel created =
                api.clubs.createReview(
                        token, new CreateReviewBodyModel(clubId, "To delete", 1, 10));

        api.clubs.deleteReview(token, created.id());
        api.clubs.getReviewNotFound(token, created.id());
    }
}
