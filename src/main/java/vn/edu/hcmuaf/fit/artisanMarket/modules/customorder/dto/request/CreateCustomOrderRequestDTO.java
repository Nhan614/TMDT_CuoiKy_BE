package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateCustomOrderRequestDTO(
    @NotNull(message = "ID nghệ nhân không được để trống") 
    Long artisanId,

    @NotBlank(message = "Tiêu đề không được để trống") 
    @Size(max = 255, message = "Tiêu đề không quá 255 ký tự") 
    String title,

    @NotBlank(message = "Mô tả không được để trống") 
    String description,

    @Positive(message = "Ngân sách phải là số dương") 
    BigDecimal budget,

    @NotNull(message = "Số lượng không được để trống") 
    @Min(value = 1, message = "Số lượng tối thiểu là 1") 
    Integer quantity,

    @Future(message = "Ngày hẹn phải ở tương lai") 
    LocalDate deadline,

    @NotEmpty(message = "Phải cung cấp ít nhất một ảnh mẫu") 
    @Size(max = 5, message = "Tối đa 5 ảnh mẫu") 
    List<String> referenceImageUrls
) {}
