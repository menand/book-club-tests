package tests;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.Fakers.FAKER;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.ClubReviewModel;
import models.clubs.CreateReviewBodyModel;
import models.clubs.ReviewsListResponseModel;
import models.clubs.UpdateReviewBodyModel;
import models.users.UserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("API")
@Tag("REGRESS")
class ClubReviewsCrudTests extends TestBase {

    private String token;
    private Integer clubId;

    @BeforeEach
    void initUserAndClub() {
        UserFixtures.TestUser user = UserFixtures.createAndLogin(api);
        token = user.token();

        ClubModel club = api.clubs.createClub(token, ClubFixtures.sampleClub());
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
        String reviewText = FAKER.lorem().sentence(10);
        int assessment = FAKER.number().numberBetween(1, 5);
        int readPages = FAKER.number().numberBetween(50, 1000);

        ClubReviewModel created =
                api.clubs.createReview(token, new CreateReviewBodyModel(clubId, reviewText, assessment, readPages));

        UserModel currentUser = api.users.getCurrentUser(token);

        step("Проверки", () -> {
            step("id присвоен (положительный)", () -> assertThat(created.id()).isPositive());
            step("club совпадает с переданным", () -> assertThat(created.club()).isEqualTo(clubId));
            step("review совпадает с переданным", () -> assertThat(created.review())
                    .isEqualTo(reviewText));
            step("assessment совпадает с переданным", () -> assertThat(created.assessment())
                    .isEqualTo(assessment));
            step("readPages совпадает с переданным", () -> assertThat(created.readPages())
                    .isEqualTo(readPages));
            step("user.id совпадает с текущим юзером",
                    () -> assertThat(created.user().id()).isEqualTo(currentUser.id()));
            step("user.username совпадает с текущим юзером",
                    () -> assertThat(created.user().username()).isEqualTo(currentUser.username()));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("Получение рецензии по id возвращает данные созданной")
    void getReviewById_returnsReviewData() {
        String reviewText = FAKER.lorem().sentence(10);
        int assessment = FAKER.number().numberBetween(1, 5);
        int readPages = FAKER.number().numberBetween(50, 1000);

        ClubReviewModel created =
                api.clubs.createReview(token, new CreateReviewBodyModel(clubId, reviewText, assessment, readPages));

        ClubReviewModel fetched = api.clubs.getReview(token, created.id());

        step("Проверки", () -> {
            step("id совпадает с созданной", () -> assertThat(fetched.id()).isEqualTo(created.id()));
            step("review совпадает с переданным", () -> assertThat(fetched.review())
                    .isEqualTo(reviewText));
            step("assessment совпадает с переданным", () -> assertThat(fetched.assessment())
                    .isEqualTo(assessment));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("Список рецензий содержит созданную")
    void getReviewsList_includesCreated() {
        ClubReviewModel created = api.clubs.createReview(token, ReviewFixtures.sampleReview(clubId));

        ReviewsListResponseModel list = api.clubs.getReviews(token);

        step("Проверки",
                () -> step("results содержит id созданной рецензии", () -> assertThat(list.results())
                        .extracting(ClubReviewModel::id)
                        .contains(created.id())));
    }

    @Test
    @Tag("SMOKE")
    @Description("PUT рецензии полностью заменяет все поля")
    void updateReviewWithPut_replacesAllFields() {
        ClubReviewModel created = api.clubs.createReview(token, ReviewFixtures.sampleReview(clubId));

        String newReview = FAKER.lorem().sentence(12);
        int newAssessment = FAKER.number().numberBetween(1, 5);
        int newPages = FAKER.number().numberBetween(100, 2000);

        ClubReviewModel updated = api.clubs.updateReview(
                token, created.id(), new CreateReviewBodyModel(clubId, newReview, newAssessment, newPages));

        step("Проверки", () -> {
            step("review обновлён", () -> assertThat(updated.review()).isEqualTo(newReview));
            step("assessment обновлён", () -> assertThat(updated.assessment()).isEqualTo(newAssessment));
            step("readPages обновлён", () -> assertThat(updated.readPages()).isEqualTo(newPages));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("PATCH рецензии обновляет только переданные поля")
    void patchReviewWithPatch_updatesPartial() {
        ClubReviewModel created = api.clubs.createReview(token, ReviewFixtures.sampleReview(clubId));

        int newAssessment = FAKER.number().numberBetween(1, 5);

        ClubReviewModel patched =
                api.clubs.patchReview(token, created.id(), new UpdateReviewBodyModel(null, null, newAssessment, null));

        step("Проверки", () -> {
            step("assessment обновлён", () -> assertThat(patched.assessment()).isEqualTo(newAssessment));
            step("review не изменён", () -> assertThat(patched.review()).isEqualTo(created.review()));
            step("readPages не изменён", () -> assertThat(patched.readPages()).isEqualTo(created.readPages()));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("DELETE рецензии возвращает 204; последующий GET — 404")
    void deleteReview_returns204_andSubsequentGetReturns404() {
        ClubReviewModel created = api.clubs.createReview(token, ReviewFixtures.sampleReview(clubId));

        api.clubs.deleteReview(token, created.id());
        api.clubs.getReviewNotFound(token, created.id());
    }
}
