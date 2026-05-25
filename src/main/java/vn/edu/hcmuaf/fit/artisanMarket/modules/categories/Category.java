package vn.edu.hcmuaf.fit.artisanMarket.modules.categories;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.Product;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // Tên danh mục, ví dụ: "Túi xách thủ công", "Trang sức handmade"

    @Column(nullable = false, unique = true, length = 100)
    private String slug; // Đường dẫn thân thiện, ví dụ: "tui-xach-thu-cong"

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả về danh mục

    @Column(name = "image_url", length = 500)
    private String imageUrl; // Ảnh đại diện cho danh mục

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Trạng thái ẩn/hiện của danh mục

    // Một danh mục có thể thuộc danh mục cha (hỗ trợ phân cấp danh mục)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Danh sách danh mục con
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children;

    // Danh sách sản phẩm thuộc danh mục này
    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
