package tests;

import static org.assertj.core.api.Assertions.assertThat;

import models.clubs.ClubModel;
import models.clubs.ClubsListResponseModel;
import org.junit.jupiter.api.Test;

class ClubsTests extends TestBase {

    @Test
    void getClubsReturns200AndValidStructure() {
        ClubsListResponseModel response = api.clubs.getClubs();
        int expectedSize = Math.min(response.count(), 50);

        assertThat(response).isNotNull();
        assertThat(response.count()).isGreaterThanOrEqualTo(0);
        assertThat(response.results()).isNotNull();
        assertThat(response.results())
                .as("размер results должен быть %d (count=%d)", expectedSize, response.count())
                .hasSize(expectedSize);
    }

    @Test
    void getClubsEachClubHasRequiredFields() {
        ClubsListResponseModel response = api.clubs.getClubs();

        for (ClubModel club : response.results()) {
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
        }
    }
}
