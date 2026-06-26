package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderPaymentStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "custom_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CustomOrderStatus status = CustomOrderStatus.PENDING;

    @Column(name = "artisan_note", columnDefinition = "TEXT")
    private String artisanNote;

    @Column(name = "quoted_price", precision = 12, scale = 2)
    private BigDecimal quotedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private CustomOrderPaymentStatus paymentStatus = CustomOrderPaymentStatus.UNPAID;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    @Column(name = "payment_at")
    private LocalDateTime paymentAt;

    @OneToMany(mappedBy = "customOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CustomOrderReferenceImage> referenceImages = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = CustomOrderStatus.PENDING;
        }
        if (this.quantity == null) {
            this.quantity = 1;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = CustomOrderPaymentStatus.UNPAID;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
