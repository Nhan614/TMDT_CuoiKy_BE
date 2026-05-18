package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanSkill;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanStatus;

import java.time.LocalDate;

@Entity
@Table(name = "artisans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Artisan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String tag;

    @Column(length = 500)
    private String image;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(length = 500)
    private String quote;

    @Column(nullable = false)
    private LocalDate startedCraftingDate;

    @Column(nullable = false)
    private Integer totalOrders = 0;

    @Column(nullable = false)
    private Integer activeOrdersCount = 0;

    @Column(nullable = false)
    private Integer maxConcurrentOrders = 5;

    @Column(nullable = false)
    private Boolean featured = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ArtisanSkill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArtisanStatus status = ArtisanStatus.ACTIVE;

//    logic
    public int getExperienceYears() {
        if (this.startedCraftingDate == null) return 0;
        return LocalDate.now().getYear() - this.startedCraftingDate.getYear();
    }

    public void promoteToFeatured() {
        if (this.rating >= 4.5 && this.status == ArtisanStatus.ACTIVE) {
            this.featured = true;
        } else {
            throw new IllegalStateException("Không đủ điều kiện làm nổi bật (Rating phải >= 4.5 và đang ACTIVE).");
        }
    }

    public void demoteFromFeatured() {
        this.featured = false;
    }

    public void acceptNewOrder() {
        if (this.status != ArtisanStatus.ACTIVE) {
            throw new IllegalStateException("Nghệ nhân hiện không ở trạng thái sẵn sàng nhận đơn.");
        }
        if (this.activeOrdersCount >= this.maxConcurrentOrders) {
            throw new IllegalStateException("Nghệ nhân đã đạt giới hạn đơn hàng tối đa.");
        }

        this.activeOrdersCount++;

        if (this.activeOrdersCount.equals(this.maxConcurrentOrders)) {
            this.status = ArtisanStatus.BUSY;
        }
    }

    public void completeOrder() {
        if (this.activeOrdersCount > 0) {
            this.activeOrdersCount--;
        }
        this.totalOrders++;

        if (this.status == ArtisanStatus.BUSY && this.activeOrdersCount < this.maxConcurrentOrders) {
            this.status = ArtisanStatus.ACTIVE;
        }
    }

    public void updateRating(Double newReviewRating) {
        if (newReviewRating < 1.0 || newReviewRating > 5.0) {
            throw new IllegalArgumentException("Điểm đánh giá phải nằm trong khoảng từ 1.0 đến 5.0");
        }

        if (this.totalOrders == 0) {
            this.rating = newReviewRating;
        } else {
            this.rating = ((this.rating * this.totalOrders) + newReviewRating) / (this.totalOrders + 1);
        }
    }

    public void changeStatus(ArtisanStatus newStatus) {
        if (this.status == ArtisanStatus.BANNED && newStatus != ArtisanStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản đang bị khóa vi phạm, liên hệ Admin để mở.");
        }
        this.status = newStatus;

        if (newStatus != ArtisanStatus.ACTIVE) {
            this.featured = false;
        }
    }
}