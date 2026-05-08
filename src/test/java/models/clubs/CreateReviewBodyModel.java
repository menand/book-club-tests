package models.clubs;

public record CreateReviewBodyModel(
        Integer club, String review, Integer assessment, Integer readPages) {}
