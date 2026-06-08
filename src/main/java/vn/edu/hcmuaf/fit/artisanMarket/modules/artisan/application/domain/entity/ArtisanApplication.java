package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.enums.ApplicationStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanSkill;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "artisan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ArtisanSkill skill;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 500)
    private String quote;

    @Column(name = "started_crafting_date", nullable = false)
    private LocalDate startedCraftingDate;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "artisan_application_proof_images", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "image_url", length = 500)
    private List<String> proofImageUrls;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.PENDING;
        }
    }
}
