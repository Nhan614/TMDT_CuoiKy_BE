package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.CustomOrder;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.CustomOrderReferenceImage;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderPaymentStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CustomOrderResponseDTO(
    Long id,
    Long userId,
    String username,
    Long artisanId,
    String artisanName,
    String artisanImage,
    String title,
    String description,
    BigDecimal budget,
    Integer quantity,
    LocalDate deadline,
    CustomOrderStatus status,
    String artisanNote,
    BigDecimal quotedPrice,
    CustomOrderPaymentStatus paymentStatus,
    String paymentTransactionId,
    LocalDateTime paymentAt,
    List<String> referenceImageUrls,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CustomOrderResponseDTO fromEntity(CustomOrder order) {
        return new CustomOrderResponseDTO(
            order.getId(),
            order.getUser().getId(),
            order.getUser().getUsername(),
            order.getArtisan().getId(),
            order.getArtisan().getName(),
            order.getArtisan().getImage(),
            order.getTitle(),
            order.getDescription(),
            order.getBudget(),
            order.getQuantity(),
            order.getDeadline(),
            order.getStatus(),
            order.getArtisanNote(),
            order.getQuotedPrice(),
            order.getPaymentStatus(),
            order.getPaymentTransactionId(),
            order.getPaymentAt(),
            order.getReferenceImages() != null ?
                order.getReferenceImages().stream().map(CustomOrderReferenceImage::getImageUrl).collect(Collectors.toList()) :
                List.of(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
