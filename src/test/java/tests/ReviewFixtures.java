package tests;

import static tests.Fakers.FAKER;

import models.clubs.CreateReviewBodyModel;

public class ReviewFixtures {

    public static CreateReviewBodyModel sampleReview(Integer clubId) {
        return new CreateReviewBodyModel(
                clubId,
                FAKER.lorem().sentence(10),
                FAKER.number().numberBetween(1, 5),
                FAKER.number().numberBetween(50, 1000));
    }
}
