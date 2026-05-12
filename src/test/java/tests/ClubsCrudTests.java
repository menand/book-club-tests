package tests;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.Fakers.FAKER;

import io.qameta.allure.Description;
import models.clubs.ClubModel;
import models.clubs.CreateClubBodyModel;
import models.clubs.UpdateClubBodyModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("CLUBS")
@Tag("REGRESS")
class ClubsCrudTests extends TestBase {

    private String token;

    @BeforeEach
    void initUser() {
        UserFixtures.TestUser user = UserFixtures.createAndLogin(api);
        token = user.token();
    }

    @AfterEach
    void cleanupUser() {
        api.users.deleteCurrentUser(token);
    }

    @Test
    @Tag("SMOKE")
    @Description("Создание клуба возвращает 201 и заполненный объект клуба")
    void createClub_returnsCreatedClub() {
        CreateClubBodyModel body = ClubFixtures.sampleClub();

        ClubModel created = api.clubs.createClub(token, body);

        step("Проверки", () -> {
            step("id присвоен (положительный)", () -> assertThat(created.id()).isPositive());
            step("bookTitle совпадает с body", () -> assertThat(created.bookTitle())
                    .isEqualTo(body.bookTitle()));
            step("bookAuthors совпадает с body", () -> assertThat(created.bookAuthors())
                    .isEqualTo(body.bookAuthors()));
            step("publicationYear совпадает с body", () -> assertThat(created.publicationYear())
                    .isEqualTo(body.publicationYear()));
            step("description совпадает с body", () -> assertThat(created.description())
                    .isEqualTo(body.description()));
            step("telegramChatLink совпадает с body", () -> assertThat(created.telegramChatLink())
                    .isEqualTo(body.telegramChatLink()));
            step("owner — положительный id", () -> assertThat(created.owner()).isPositive());
            step("members содержит только owner", () -> assertThat(created.members())
                    .containsExactly(created.owner()));
            step("reviews пустой", () -> assertThat(created.reviews()).isEmpty());
            step("created — timestamp в ISO 8601 (UTC)", () -> assertThat(created.created())
                    .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z"));
            step("modified не задан после создания", () -> assertThat(created.modified())
                    .isNull());
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("Получение клуба по id возвращает данные созданного клуба")
    void getClubById_returnsClubData() {
        ClubModel created = api.clubs.createClub(token, ClubFixtures.sampleClub());

        ClubModel fetched = api.clubs.getClub(token, created.id());

        step("Проверки", () -> {
            step("id совпадает с созданным", () -> assertThat(fetched.id()).isEqualTo(created.id()));
            step("bookTitle совпадает с созданным", () -> assertThat(fetched.bookTitle())
                    .isEqualTo(created.bookTitle()));
            step("bookAuthors совпадает с созданным", () -> assertThat(fetched.bookAuthors())
                    .isEqualTo(created.bookAuthors()));
            step("publicationYear совпадает с созданным", () -> assertThat(fetched.publicationYear())
                    .isEqualTo(created.publicationYear()));
            step("description совпадает с созданным", () -> assertThat(fetched.description())
                    .isEqualTo(created.description()));
            step("telegramChatLink совпадает с созданным", () -> assertThat(fetched.telegramChatLink())
                    .isEqualTo(created.telegramChatLink()));
            step("owner совпадает с созданным", () -> assertThat(fetched.owner())
                    .isEqualTo(created.owner()));
            step("members совпадает с созданным", () -> assertThat(fetched.members())
                    .isEqualTo(created.members()));
            step("reviews совпадает с созданным", () -> assertThat(fetched.reviews())
                    .isEqualTo(created.reviews()));
            step("created совпадает с созданным", () -> assertThat(fetched.created())
                    .isEqualTo(created.created()));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("PUT клуба полностью заменяет все поля")
    void updateClubWithPut_replacesAllFields() {
        ClubModel created = api.clubs.createClub(token, ClubFixtures.sampleClub());

        CreateClubBodyModel updateBody = ClubFixtures.sampleClub();
        ClubModel updated = api.clubs.updateClub(token, created.id(), updateBody);

        step("Проверки", () -> {
            step("id не изменился", () -> assertThat(updated.id()).isEqualTo(created.id()));
            step("bookTitle обновлён", () -> assertThat(updated.bookTitle()).isEqualTo(updateBody.bookTitle()));
            step("bookAuthors обновлён", () -> assertThat(updated.bookAuthors()).isEqualTo(updateBody.bookAuthors()));
            step("publicationYear обновлён", () -> assertThat(updated.publicationYear())
                    .isEqualTo(updateBody.publicationYear()));
            step("description обновлён", () -> assertThat(updated.description()).isEqualTo(updateBody.description()));
            step("telegramChatLink обновлён", () -> assertThat(updated.telegramChatLink())
                    .isEqualTo(updateBody.telegramChatLink()));
            step("bookTitle отличается от исходного", () -> assertThat(updated.bookTitle())
                    .isNotEqualTo(created.bookTitle()));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("PATCH клуба обновляет только переданные поля")
    void patchClubWithPatch_updatesPartialFields() {
        CreateClubBodyModel originalBody = ClubFixtures.sampleClub();
        ClubModel created = api.clubs.createClub(token, originalBody);

        String patchedDescription = FAKER.lorem().sentence(10);

        ClubModel patched = api.clubs.patchClub(
                token, created.id(), new UpdateClubBodyModel(null, null, null, patchedDescription, null));

        step("Проверки", () -> {
            step("description обновлён", () -> assertThat(patched.description()).isEqualTo(patchedDescription));
            step("bookTitle не изменился", () -> assertThat(patched.bookTitle()).isEqualTo(originalBody.bookTitle()));
            step("bookAuthors не изменился", () -> assertThat(patched.bookAuthors())
                    .isEqualTo(originalBody.bookAuthors()));
            step("publicationYear не изменился", () -> assertThat(patched.publicationYear())
                    .isEqualTo(originalBody.publicationYear()));
            step("telegramChatLink не изменился", () -> assertThat(patched.telegramChatLink())
                    .isEqualTo(originalBody.telegramChatLink()));
        });
    }

    @Test
    @Tag("SMOKE")
    @Description("DELETE клуба возвращает 204, последующий GET — 404")
    void deleteClub_returns204_andSubsequentGetReturns404() {
        ClubModel created = api.clubs.createClub(token, ClubFixtures.sampleClub());

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
        api.clubs.createClubUnauthorized(ClubFixtures.sampleClub());
    }
}
