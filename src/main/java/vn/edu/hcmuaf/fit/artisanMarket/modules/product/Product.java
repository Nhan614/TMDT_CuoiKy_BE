package vn.edu.hcmuaf.fit.artisanMarket.modules.product;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String slug; // Đường dẫn thân thiện dạng: "tui-len-handmade-hoa-tra"

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết sản phẩm hoặc câu chuyện làm ra nó

    @Column(name = "short_description", length = 500)
    private String shortDescription; // Mô tả ngắn hiển thị ở danh sách sản phẩm

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Giá gốc sản phẩm

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice; // Giá sau khi giảm (nếu có khuyến mãi)

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity; // Số lượng hàng có sẵn trong kho

    // --- ĐẶC THÙ ĐỒ HANDMADE ---
    @Column(name = "is_pre_order", nullable = false)
    private boolean isPreOrder = false; // Có cho phép đặt hàng trước khi hết hàng sẵn không

    @Column(name = "making_days")
    private Integer makingDays; // Số ngày ước tính để làm xong sản phẩm (ví dụ: từ 3-5 ngày)

    @Column(name = "artisan_name", length = 100)
    private String artisanName; // Tên của nghệ nhân/thợ thủ công tạo ra sản phẩm này
    // ----------------------------

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl; // Ảnh đại diện chính hiển thị ngoài trang chủ

    // Lưu danh sách các ảnh chi tiết của sản phẩm vào một bảng phụ (mapping tự
    // động)
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;

    // Lưu danh sách chất liệu (ví dụ: ["Len Cotton", "Hạt cườm", "Vải lanh"])
    @ElementCollection
    @CollectionTable(name = "product_materials", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "material")
    private List<String> materials;

    // Mối quan hệ Nhiều sản phẩm thuộc Một danh mục (Category)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "average_rating")
    private Double averageRating = 0.0; // Điểm đánh giá trung bình (1.0 -> 5.0)

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Trạng thái kích hoạt (ẩn/hiển thị trên web)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Tự động gán thời gian khi tạo mới sản phẩm
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Tự động cập nhật thời gian khi sửa đổi sản phẩm
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}