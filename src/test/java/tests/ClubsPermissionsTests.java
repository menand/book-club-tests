package tests;

import static tests.Fakers.FAKER;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.UpdateClubBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubsPermissionsTests extends TestBase {

    private String ownerToken;
    private String otherToken;
    private Integer clubId;

    @BeforeEach
    void initUsersAndClub() {
        UserFixtures.TestUser owner = UserFixtures.createAndLogin(api);
        ownerToken = owner.token();

        UserFixtures.TestUser other = UserFixtures.createAndLogin(api);
        otherToken = other.token();

        ClubModel club = api.clubs.createClub(ownerToken, ClubFixtures.sampleClub());
        clubId = club.id();
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
    @Description("PUT /clubs/{id}/ чужим юзером — ожидается 403")
    void updateClubByOtherUser_returns403() {
        api.clubs.updateClubForbidden(otherToken, clubId, ClubFixtures.sampleClub());
    }

    @Test
    @Description("PATCH /clubs/{id}/ чужим юзером — ожидается 403")
    void patchClubByOtherUser_returns403() {
        api.clubs.patchClubForbidden(
                otherToken,
                clubId,
                new UpdateClubBodyModel(null, null, null, FAKER.lorem().sentence(5), null));
    }

    @Test
    @Description("DELETE /clubs/{id}/ чужим юзером — ожидается 403")
    void deleteClubByOtherUser_returns403() {
        api.clubs.deleteClubForbidden(otherToken, clubId);
    }
}
