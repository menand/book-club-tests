package tests;

import static tests.Fakers.FAKER;
import static tests.Fakers.shortUid;

import models.clubs.CreateClubBodyModel;

public class ClubFixtures {

    public static CreateClubBodyModel sampleClub() {
        String uniqueSuffix = shortUid();
        return new CreateClubBodyModel(
                FAKER.book().title() + " #" + uniqueSuffix,
                FAKER.book().author(),
                FAKER.number().numberBetween(1900, 2025),
                FAKER.lorem().sentence(15),
                "https://t.me/club_" + uniqueSuffix);
    }
}
