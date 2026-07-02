package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.domain.repository.CategoryRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.model.Category;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service.CategoryService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // ─── Helper: DTO ───────────────────────────────────────────────────────────

    private CategoryResponseDTO toDTO(Category category) {
        CategoryResponseDTO dto = CategoryResponseDTO.fromEntity(category);
        if (dto != null) {
            long count = productRepository.countActiveProductsByCategoryId(category.getId());
            dto.setProductCount(count);
        }
        return dto;
    }

    // ─── Helper: Slug generation ────────────────────────────────────────────────

    /**
     * Sinh slug từ name: normalize Unicode (NFKD) → bỏ dấu → lowercase
     * → thay khoảng trắng & ký tự đặc biệt bằng "-" → trim dấu "-" đầu/cuối.
     */
    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFKD);
        // Loại bỏ combining diacritical marks (dấu tiếng Việt)
        String noAccent = normalized.replaceAll("\\p{M}", "");
        String lower = noAccent.toLowerCase();
        // Thay đặc biệt / khoảng trắng → dấu gạch ngang
        String slug = lower.replaceAll("[^a-z0-9]+", "-");
        // Trim đầu/cuối
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }

    /**
     * Đảm bảo slug là duy nhất: nếu trùng thì thêm suffix -2, -3, …
     */
    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;
        while (true) {
            boolean exists = (excludeId == null)
                    ? categoryRepository.existsBySlug(slug)
                    : categoryRepository.existsBySlugAndIdNot(slug, excludeId);
            if (!exists) return slug;
            counter++;
            slug = baseSlug + "-" + counter;
        }
    }

    // ─── Helper: Cycle detection ────────────────────────────────────────────────

    /**
     * Kiểm tra xem potentialParentId có phải là con (hoặc cháu) của categoryId không.
     * Dùng để chặn vòng lặp A→B→A khi cập nhật parentId.
     */
    private boolean wouldCreateCycle(Long categoryId, Long potentialParentId) {
        if (potentialParentId == null) return false;
        Long current = potentialParentId;
        // Traverse up the hierarchy
        while (current != null) {
            if (current.equals(categoryId)) return true;
            Category cat = categoryRepository.findById(current).orElse(null);
            if (cat == null || cat.getParent() == null) break;
            current = cat.getParent().getId();
        }
        return false;
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        // Kiểm tra tên bắt buộc
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Tên danh mục không được để trống");
        }

        // Kiểm tra tên trùng
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        // Sinh slug nếu FE không gửi
        String slug;
        if (request.getSlug() == null || request.getSlug().isBlank()) {
            slug = generateSlug(request.getName());
        } else {
            slug = request.getSlug();
        }
        slug = ensureUniqueSlug(slug, null);

        // Kiểm tra slug trùng (trường hợp FE gửi slug thủ công)
        if (!slug.equals(generateSlug(request.getName())) && categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Đường dẫn thân thiện (slug) của danh mục đã tồn tại");
        }

        // Tìm category cha
        Category parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(parent)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return toDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        return toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> getAllCategories(int page, int size, String search, Long parentId,
            Boolean isActive, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "name");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "name");
                    break;
                case "id_desc":
                    sort = Sort.by(Sort.Direction.DESC, "id");
                    break;
                default:
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size, sort);
        Page<Category> categories = categoryRepository.findCategories(
                (search != null && search.isBlank()) ? null : search,
                parentId,
                isActive,
                pageable);
        return categories.map(this::toDTO);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // Cập nhật tên
        if (request.getName() != null && !request.getName().isBlank()
                && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new RuntimeException("Tên danh mục đã tồn tại");
            }
            category.setName(request.getName());
        }

        // Cập nhật slug (hoặc tự sinh lại từ tên mới)
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            if (!request.getSlug().equals(category.getSlug())) {
                if (categoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                    throw new RuntimeException("Đường dẫn thân thiện (slug) của danh mục đã tồn tại");
                }
                category.setSlug(request.getSlug());
            }
        } else if (request.getName() != null && !request.getName().equals(category.getName())) {
            // Nếu tên thay đổi mà không gửi slug → tự sinh slug mới
            String newSlug = ensureUniqueSlug(generateSlug(request.getName()), id);
            category.setSlug(newSlug);
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        // Cập nhật parent + kiểm tra vòng lặp
        if (request.getParentId() != null) {
            if (request.getParentId() == -1L) {
                // Giá trị -1 = xóa parent (thành root category)
                category.setParent(null);
            } else if (request.getParentId().equals(id)) {
                throw new RuntimeException("Danh mục không thể là cha của chính nó");
            } else if (wouldCreateCycle(id, request.getParentId())) {
                throw new RuntimeException("Không thể tạo vòng lặp phân cấp: danh mục cha đã là con của danh mục hiện tại");
            } else {
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại"));
                category.setParent(parent);
            }
        }

        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }

        return toDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // Kiểm tra có category con không
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa danh mục đang có " + category.getChildren().size()
                    + " danh mục con. Vui lòng xóa hoặc chuyển danh mục con trước, hoặc sử dụng chức năng Ẩn thay vì xóa.");
        }

        // Kiểm tra có sản phẩm không
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa danh mục đang chứa " + category.getProducts().size()
                    + " sản phẩm. Vui lòng chuyển các sản phẩm sang danh mục khác trước, hoặc sử dụng chức năng Ẩn thay vì xóa.");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getSimpleCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
