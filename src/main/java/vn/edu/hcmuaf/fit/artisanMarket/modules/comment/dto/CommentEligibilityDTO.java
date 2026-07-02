package vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto;

public record CommentEligibilityDTO(
        boolean hasPurchased,
        boolean hasReviewed,
        boolean canReview
) {}
