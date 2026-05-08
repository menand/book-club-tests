package api;

import static io.restassured.RestAssured.given;
import static specs.clubs.ClubsSpec.clubNoContentResponseSpec;
import static specs.clubs.ClubsSpec.clubNotFoundResponseSpec;
import static specs.clubs.ClubsSpec.clubUnauthorizedResponseSpec;
import static specs.clubs.ClubsSpec.clubsRequestSpec;
import static specs.clubs.ClubsSpec.createdClubResponseSpec;
import static specs.clubs.ClubsSpec.createdReviewResponseSpec;
import static specs.clubs.ClubsSpec.successfulClubResponseSpec;
import static specs.clubs.ClubsSpec.successfulClubsListResponseSpec;
import static specs.clubs.ClubsSpec.successfulReviewResponseSpec;
import static specs.clubs.ClubsSpec.successfulReviewsListResponseSpec;
import static specs.users.UserSpec.authRequestSpec;

import io.qameta.allure.Step;
import models.clubs.ClubModel;
import models.clubs.ClubReviewModel;
import models.clubs.ClubsListResponseModel;
import models.clubs.CreateClubBodyModel;
import models.clubs.CreateReviewBodyModel;
import models.clubs.ReviewsListResponseModel;
import models.clubs.UpdateClubBodyModel;
import models.clubs.UpdateReviewBodyModel;

public class ClubsApiClient {

    @Step("Получение списка клубов GET /clubs/")
    public ClubsListResponseModel getClubs() {
        return given(clubsRequestSpec)
                .when()
                .get("/clubs/")
                .then()
                .spec(successfulClubsListResponseSpec)
                .extract()
                .as(ClubsListResponseModel.class);
    }

    @Step("Создание клуба POST /clubs/")
    public ClubModel createClub(String token, CreateClubBodyModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .post("/clubs/")
                .then()
                .spec(createdClubResponseSpec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("Получение клуба GET /clubs/{id}/")
    public ClubModel getClub(String token, Integer id) {
        return given(authRequestSpec(token))
                .when()
                .get("/clubs/{id}/", id)
                .then()
                .spec(successfulClubResponseSpec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("Полное обновление клуба PUT /clubs/{id}/")
    public ClubModel updateClub(String token, Integer id, CreateClubBodyModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .put("/clubs/{id}/", id)
                .then()
                .spec(successfulClubResponseSpec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("Частичное обновление клуба PATCH /clubs/{id}/")
    public ClubModel patchClub(String token, Integer id, UpdateClubBodyModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .patch("/clubs/{id}/", id)
                .then()
                .spec(successfulClubResponseSpec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("Удаление клуба DELETE /clubs/{id}/")
    public void deleteClub(String token, Integer id) {
        given(authRequestSpec(token))
                .when()
                .delete("/clubs/{id}/", id)
                .then()
                .spec(clubNoContentResponseSpec);
    }

    @Step("Получение несуществующего клуба — ожидается 404")
    public void getClubNotFound(String token, Integer id) {
        given(authRequestSpec(token))
                .when()
                .get("/clubs/{id}/", id)
                .then()
                .spec(clubNotFoundResponseSpec);
    }

    @Step("Создание клуба без авторизации — ожидается 401")
    public void createClubUnauthorized(CreateClubBodyModel body) {
        given(clubsRequestSpec)
                .body(body)
                .when()
                .post("/clubs/")
                .then()
                .spec(clubUnauthorizedResponseSpec);
    }

    @Step("Вступление в клуб POST /clubs/{id}/members/me/")
    public void joinClub(String token, Integer clubId) {
        given(authRequestSpec(token))
                .when()
                .post("/clubs/{id}/members/me/", clubId)
                .then()
                .spec(clubNoContentResponseSpec);
    }

    @Step("Выход из клуба DELETE /clubs/{id}/members/me/")
    public void leaveClub(String token, Integer clubId) {
        given(authRequestSpec(token))
                .when()
                .delete("/clubs/{id}/members/me/", clubId)
                .then()
                .spec(clubNoContentResponseSpec);
    }

    @Step("Создание рецензии POST /clubs/reviews/")
    public ClubReviewModel createReview(String token, CreateReviewBodyModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .post("/clubs/reviews/")
                .then()
                .spec(createdReviewResponseSpec)
                .extract()
                .as(ClubReviewModel.class);
    }

    @Step("Получение списка рецензий GET /clubs/reviews/")
    public ReviewsListResponseModel getReviews(String token) {
        return given(authRequestSpec(token))
                .when()
                .get("/clubs/reviews/")
                .then()
                .spec(successfulReviewsListResponseSpec)
                .extract()
                .as(ReviewsListResponseModel.class);
    }

    @Step("Получение рецензии GET /clubs/reviews/{id}/")
    public ClubReviewModel getReview(String token, Integer id) {
        return given(authRequestSpec(token))
                .when()
                .get("/clubs/reviews/{id}/", id)
                .then()
                .spec(successfulReviewResponseSpec)
                .extract()
                .as(ClubReviewModel.class);
    }

    @Step("Полное обновление рецензии PUT /clubs/reviews/{id}/")
    public ClubReviewModel updateReview(String token, Integer id, CreateReviewBodyModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .put("/clubs/reviews/{id}/", id)
                .then()
                .spec(successfulReviewResponseSpec)
                .extract()
                .as(ClubReviewModel.class);
    }

    @Step("Частичное обновление рецензии PATCH /clubs/reviews/{id}/")
    public ClubReviewModel patchReview(String token, Integer id, UpdateReviewBodyModel body) {
        return given(authRequestSpec(token))
                .body(body)
                .when()
                .patch("/clubs/reviews/{id}/", id)
                .then()
                .spec(successfulReviewResponseSpec)
                .extract()
                .as(ClubReviewModel.class);
    }

    @Step("Удаление рецензии DELETE /clubs/reviews/{id}/")
    public void deleteReview(String token, Integer id) {
        given(authRequestSpec(token))
                .when()
                .delete("/clubs/reviews/{id}/", id)
                .then()
                .spec(clubNoContentResponseSpec);
    }

    @Step("Получение несуществующей рецензии — ожидается 404")
    public void getReviewNotFound(String token, Integer id) {
        given(authRequestSpec(token))
                .when()
                .get("/clubs/reviews/{id}/", id)
                .then()
                .spec(clubNotFoundResponseSpec);
    }
}
