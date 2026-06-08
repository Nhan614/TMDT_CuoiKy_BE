package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.response;

import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.ArtisanApplication;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ArtisanApplicationResponseDTO(
        Long id,
        Long userId,
        String fullName,
        String skill,
        String skillDisplayName,
        String bio,
        String quote,
        LocalDate startedCraftingDate,
        String portfolioUrl,
        String avatarUrl,
        List<String> proofImageUrls,
        ApplicationStatus status,
        String rejectionReason,
        Long reviewedBy,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
    public static ArtisanApplicationResponseDTO fromEntity(ArtisanApplication app) {
        if (app == null) return null;
        return ArtisanApplicationResponseDTO.builder()
                .id(app.getId())
                .userId(app.getUserId())
                .fullName(app.getFullName())
                .skill(app.getSkill() != null ? app.getSkill().name() : null)
                .skillDisplayName(app.getSkill() != null ? app.getSkill().getDisplayName() : null)
                .bio(app.getBio())
                .quote(app.getQuote())
                .startedCraftingDate(app.getStartedCraftingDate())
                .portfolioUrl(app.getPortfolioUrl())
                .avatarUrl(app.getAvatarUrl())
                .proofImageUrls(app.getProofImageUrls())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                .reviewedBy(app.getReviewedBy())
                .createdAt(app.getCreatedAt())
                .reviewedAt(app.getReviewedAt())
                .build();
    }
}
