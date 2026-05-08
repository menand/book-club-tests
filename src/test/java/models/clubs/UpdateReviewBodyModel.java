package models.clubs;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateReviewBodyModel(
        Integer club, String review, Integer assessment, Integer readPages) {}
