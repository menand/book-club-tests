package tests;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.ClubsListResponseModel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubsTests extends TestBase {

    @Test
    @Tag("SMOKE")
    @Description("Получение списка клубов с корректной структурой ответа")
    void getClubsReturns200AndValidStructure() {
        ClubsListResponseModel response = api.clubs.getClubs();
        int expectedSize = Math.min(response.count(), 50);

        step("Проверки", () -> {
            step("ответ не null", () -> assertThat(response).isNotNull());
            step("count >= 0", () -> assertThat(response.count()).isGreaterThanOrEqualTo(0));
            step("results не null", () -> assertThat(response.results()).isNotNull());
            step("размер results = min(count, 50)", () -> assertThat(response.results())
                    .as("размер results должен быть %d (count=%d)", expectedSize, response.count())
                    .hasSize(expectedSize));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("Каждый клуб в списке содержит все обязательные поля")
    void getClubsEachClubHasRequiredFields() {
        ClubsListResponseModel response = api.clubs.getClubs();

        step("Проверки: каждый клуб имеет все обязательные поля", () -> {
            for (ClubModel club : response.results()) {
                step("клуб id=" + club.id() + " — поля заполнены", () -> {
                    assertThat(club.id()).isNotNull().isPositive();
                    assertThat(club.bookTitle()).isNotNull();
                    assertThat(club.bookAuthors()).isNotNull();
                    assertThat(club.publicationYear()).isNotNull();
                    assertThat(club.description()).isNotNull();
                    assertThat(club.telegramChatLink()).isNotNull();
                    assertThat(club.owner()).isNotNull().isPositive();
                    assertThat(club.members()).isNotNull();
                    assertThat(club.reviews()).isNotNull();
                    assertThat(club.created()).isNotNull();
                });
            }
        });
    }
}
