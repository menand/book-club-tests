package tests;

import static tests.Fakers.FAKER;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.ClubReviewModel;
import models.clubs.CreateReviewBodyModel;
import models.clubs.UpdateReviewBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubReviewsPermissionsTests extends TestBase {

    private String ownerToken;
    private String otherToken;
    private Integer clubId;
    private Integer reviewId;

    @BeforeEach
    void initUsersClubAndReview() {
        UserFixtures.TestUser owner = UserFixtures.createAndLogin(api);
        ownerToken = owner.token();

        UserFixtures.TestUser other = UserFixtures.createAndLogin(api);
        otherToken = other.token();

        ClubModel club = api.clubs.createClub(ownerToken, ClubFixtures.sampleClub());
        clubId = club.id();

        ClubReviewModel review = api.clubs.createReview(ownerToken, ReviewFixtures.sampleReview(clubId));
        reviewId = review.id();
    }

    @AfterEach
    void cleanupUsers() {
        try {
            if (otherToken != null) {
                api.users.deleteCurrentUser(otherToken);
            }
        } finally {
            if (ownerToken != null) {
                api.users.deleteCurrentUser(ownerToken);
            }
        }
    }

    @Test
    @Description("POST /clubs/reviews/ без авторизации — ожидается 401")
    void createReviewWithoutAuth_returns401() {
        api.clubs.createReviewUnauthorized(ReviewFixtures.sampleReview(clubId));
    }

    @Test
    @Description("PUT /clubs/reviews/{id}/ без авторизации — ожидается 401")
    void updateReviewWithoutAuth_returns401() {
        api.clubs.updateReviewUnauthorized(reviewId, ReviewFixtures.sampleReview(clubId));
    }

    @Test
    @Description("PATCH /clubs/reviews/{id}/ без авторизации — ожидается 401")
    void patchReviewWithoutAuth_returns401() {
        api.clubs.patchReviewUnauthorized(
                reviewId, new UpdateReviewBodyModel(null, null, FAKER.number().numberBetween(1, 5), null));
    }

    @Test
    @Description("DELETE /clubs/reviews/{id}/ без авторизации — ожидается 401")
    void deleteReviewWithoutAuth_returns401() {
        api.clubs.deleteReviewUnauthorized(reviewId);
    }

    @Test
    @Description("PUT /clubs/reviews/{id}/ чужим юзером — ожидается 403")
    void updateReviewByOtherUser_returns403() {
        api.clubs.updateReviewForbidden(otherToken, reviewId, ReviewFixtures.sampleReview(clubId));
    }

    @Test
    @Description("PATCH /clubs/reviews/{id}/ чужим юзером — ожидается 403")
    void patchReviewByOtherUser_returns403() {
        api.clubs.patchReviewForbidden(
                otherToken,
                reviewId,
                new UpdateReviewBodyModel(null, FAKER.lorem().sentence(8), null, null));
    }

    @Test
    @Description("DELETE /clubs/reviews/{id}/ чужим юзером — ожидается 403")
    void deleteReviewByOtherUser_returns403() {
        api.clubs.deleteReviewForbidden(otherToken, reviewId);
    }

    @Test
    @Description("POST /clubs/reviews/ с assessment > 5 — ожидается 400")
    void createReviewWithAssessmentAboveMax_returns400() {
        api.clubs.createReviewWithBadRequest(
                ownerToken,
                new CreateReviewBodyModel(
                        clubId, FAKER.lorem().sentence(8), 6, FAKER.number().numberBetween(50, 500)));
    }

    @Test
    @Description("POST /clubs/reviews/ с assessment < 1 — ожидается 400")
    void createReviewWithAssessmentBelowMin_returns400() {
        api.clubs.createReviewWithBadRequest(
                ownerToken,
                new CreateReviewBodyModel(
                        clubId, FAKER.lorem().sentence(8), 0, FAKER.number().numberBetween(50, 500)));
    }

    @Test
    @Description("POST /clubs/reviews/ с несуществующим clubId — ожидается 400")
    void createReviewForNonExistentClub_returns400() {
        api.clubs.createReviewWithBadRequest(
                ownerToken,
                new CreateReviewBodyModel(
                        999_999_999,
                        FAKER.lorem().sentence(8),
                        FAKER.number().numberBetween(1, 5),
                        FAKER.number().numberBetween(50, 500)));
    }
}
