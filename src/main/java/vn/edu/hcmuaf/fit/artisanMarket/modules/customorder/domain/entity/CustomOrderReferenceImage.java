package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_order_reference_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomOrderReferenceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_order_id", nullable = false)
    private CustomOrder customOrder;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "public_id", length = 300)
    private String publicId;
}
