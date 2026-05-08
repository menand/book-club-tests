package models.clubs;

import java.util.List;

public record ReviewsListResponseModel(
        Integer count, String next, String previous, List<ClubReviewModel> results) {}
