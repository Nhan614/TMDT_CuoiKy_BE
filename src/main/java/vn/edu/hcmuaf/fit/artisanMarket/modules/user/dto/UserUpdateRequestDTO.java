package vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {
    private String fullName;
    private String email;
    private String phone;
    private String currentPassword;
    private String newPassword;
}
