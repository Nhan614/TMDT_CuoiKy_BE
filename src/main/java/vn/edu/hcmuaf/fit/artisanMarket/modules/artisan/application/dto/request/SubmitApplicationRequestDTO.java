package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanSkill;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SubmitApplicationRequestDTO(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @NotNull(message = "Kỹ năng không được để trống")
        ArtisanSkill skill,

        String bio,

        @Size(max = 500, message = "Quote tối đa 500 ký tự")
        String quote,

        @NotNull(message = "Ngày bắt đầu làm nghề không được để trống")
        @PastOrPresent(message = "Ngày bắt đầu làm nghề không được ở tương lai")
        LocalDate startedCraftingDate,

        @Size(max = 500, message = "Link portfolio tối đa 500 ký tự")
        String portfolioUrl,

        @NotBlank(message = "Ảnh đại diện không được để trống")
        @Size(max = 500, message = "Link ảnh đại diện tối đa 500 ký tự")
        String avatarUrl,

        @NotEmpty(message = "Phải có ít nhất 1 ảnh minh chứng")
        @Size(max = 5, message = "Tối đa 5 ảnh minh chứng")
        List<String> proofImageUrls
) {
}
