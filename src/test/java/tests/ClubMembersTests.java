package tests;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
import java.util.UUID;
import models.clubs.ClubModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.users.UserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubMembersTests extends TestBase {

    private Integer clubId;
    private String ownerToken;
    private String memberToken;
    private Integer memberId;

    @BeforeEach
    void initOwnerAndMember() {
        String ownerUid = UUID.randomUUID().toString().substring(0, 8);
        String memberUid = UUID.randomUUID().toString().substring(0, 8);

        api.users.register(new RegistrationBodyModel("user_" + ownerUid, "pass_" + ownerUid));
        ownerToken = api.auth.loginAndGetAccessToken(new LoginBodyModel("user_" + ownerUid, "pass_" + ownerUid));

        ClubModel club = api.clubs.createClub(ownerToken, ClubFixtures.sampleClub(ownerUid));
        clubId = club.id();

        api.users.register(new RegistrationBodyModel("user_" + memberUid, "pass_" + memberUid));
        memberToken = api.auth.loginAndGetAccessToken(new LoginBodyModel("user_" + memberUid, "pass_" + memberUid));

        UserModel member = api.users.getCurrentUser(memberToken);
        memberId = member.id();
    }

    @AfterEach
    void cleanupUsers() {
        try {
            if (memberToken != null) {
                api.users.deleteCurrentUser(memberToken);
            }
        } finally {
            if (ownerToken != null) {
                api.users.deleteCurrentUser(ownerToken);
            }
        }
    }

    @Test
    @Tag("SMOKE")
    @Description("После join текущий пользователь появляется в members клуба")
    void joinClub_addsCurrentUserToMembers() {
        api.clubs.joinClub(memberToken, clubId);

        ClubModel club = api.clubs.getClub(memberToken, clubId);

        step("Проверки",
                () -> step("members содержит id юзера, который сделал join", () -> assertThat(club.members())
                        .contains(memberId)));
    }

    @Test
    @Tag("SMOKE")
    @Description("После leave текущий пользователь исчезает из members клуба")
    void leaveClub_removesCurrentUserFromMembers() {
        api.clubs.joinClub(memberToken, clubId);
        ClubModel beforeLeave = api.clubs.getClub(memberToken, clubId);
        step("Проверка предусловия",
                () -> step("member в списке после join", () -> assertThat(beforeLeave.members())
                        .contains(memberId)));

        api.clubs.leaveClub(memberToken, clubId);

        ClubModel afterLeave = api.clubs.getClub(memberToken, clubId);
        step("Проверки", () -> {
            step("members не содержит id юзера, который сделал leave", () -> assertThat(afterLeave.members())
                    .doesNotContain(memberId));
            step("размер members уменьшился ровно на 1", () -> assertThat(afterLeave.members())
                    .hasSize(beforeLeave.members().size() - 1));
        });
    }
}
