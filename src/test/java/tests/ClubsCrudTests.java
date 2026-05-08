package tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
import java.util.UUID;
import models.clubs.ClubModel;
import models.clubs.CreateClubBodyModel;
import models.clubs.UpdateClubBodyModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubsCrudTests extends TestBase {

    private String token;
    private String uid;

    @BeforeEach
    void initUser() {
        uid = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + uid;
        String password = "pass_" + uid;

        api.users.register(new RegistrationBodyModel(username, password));
        token = api.auth.loginAndGetAccessToken(new LoginBodyModel(username, password));
    }

    @AfterEach
    void cleanupUser() {
        api.users.deleteCurrentUser(token);
    }

    @Test
    @Tag("SMOKE")
    @Description("Создание клуба возвращает 201 и заполненный объект клуба")
    void createClub_returnsCreatedClub() {
        CreateClubBodyModel body = ClubFixtures.sampleClub(uid);

        ClubModel created = api.clubs.createClub(token, body);

        assertThat(created.id()).isPositive();
        assertThat(created.bookTitle()).isEqualTo(body.bookTitle());
        assertThat(created.bookAuthors()).isEqualTo(body.bookAuthors());
        assertThat(created.publicationYear()).isEqualTo(body.publicationYear());
        assertThat(created.description()).isEqualTo(body.description());
        assertThat(created.telegramChatLink()).isEqualTo(body.telegramChatLink());
        assertThat(created.owner()).isPositive();
        assertThat(created.members()).isNotNull();
        assertThat(created.reviews()).isNotNull();
    }

    @Test
    @Tag("SMOKE")
    @Description("Получение клуба по id возвращает данные созданного клуба")
    void getClubById_returnsClubData() {
        ClubModel created = api.clubs.createClub(token, ClubFixtures.sampleClub(uid));

        ClubModel fetched = api.clubs.getClub(token, created.id());

        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.bookTitle()).isEqualTo(created.bookTitle());
        assertThat(fetched.owner()).isEqualTo(created.owner());
    }

    @Test
    @Tag("SMOKE")
    @Description("PUT клуба полностью заменяет все поля")
    void updateClubWithPut_replacesAllFields() {
        ClubModel created = api.clubs.createClub(token, ClubFixtures.sampleClub(uid));

        String updatedTitle = "Updated Book " + uid;
        String updatedAuthor = "Updated Author " + uid;
        String updatedDescription = "Updated description " + uid;
        String updatedLink = "https://t.me/updated_" + uid;

        CreateClubBodyModel updateBody =
                new CreateClubBodyModel(
                        updatedTitle, updatedAuthor, 1999, updatedDescription, updatedLink);

        ClubModel updated = api.clubs.updateClub(token, created.id(), updateBody);

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.bookTitle()).isEqualTo(updatedTitle);
        assertThat(updated.bookAuthors()).isEqualTo(updatedAuthor);
        assertThat(updated.publicationYear()).isEqualTo(1999);
        assertThat(updated.description()).isEqualTo(updatedDescription);
        assertThat(updated.telegramChatLink()).isEqualTo(updatedLink);
    }

    @Test
    @Tag("SMOKE")
    @Description("PATCH клуба обновляет только переданные поля")
    void patchClubWithPatch_updatesPartialFields() {
        CreateClubBodyModel originalBody = ClubFixtures.sampleClub(uid);
        ClubModel created = api.clubs.createClub(token, originalBody);

        ClubModel patched =
                api.clubs.patchClub(
                        token,
                        created.id(),
                        new UpdateClubBodyModel(null, null, null, "Patched description", null));

        assertThat(patched.description()).isEqualTo("Patched description");
        assertThat(patched.bookTitle()).isEqualTo(originalBody.bookTitle());
        assertThat(patched.bookAuthors()).isEqualTo(originalBody.bookAuthors());
        assertThat(patched.publicationYear()).isEqualTo(originalBody.publicationYear());
        assertThat(patched.telegramChatLink()).isEqualTo(originalBody.telegramChatLink());
    }

    @Test
    @Tag("SMOKE")
    @Description("DELETE клуба возвращает 204, последующий GET — 404")
    void deleteClub_returns204_andSubsequentGetReturns404() {
        ClubModel created = api.clubs.createClub(token, ClubFixtures.sampleClub(uid));

        api.clubs.deleteClub(token, created.id());
        api.clubs.getClubNotFound(token, created.id());
    }

    @Test
    @Description("GET несуществующего клуба возвращает 404")
    void getNonExistentClub_returns404() {
        api.clubs.getClubNotFound(token, 999_999_999);
    }

    @Test
    @Description("POST /clubs/ без авторизации возвращает 401")
    void createClubWithoutAuth_returns401() {
        api.clubs.createClubUnauthorized(ClubFixtures.sampleClub(uid));
    }
}
