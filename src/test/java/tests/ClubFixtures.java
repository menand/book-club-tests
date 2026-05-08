package tests;

import models.clubs.CreateClubBodyModel;

class ClubFixtures {

    static CreateClubBodyModel sampleClub(String uid) {
        return new CreateClubBodyModel(
                "Book " + uid,
                "Author " + uid,
                2024,
                "Description " + uid,
                "https://t.me/bookclub_" + uid);
    }
}
