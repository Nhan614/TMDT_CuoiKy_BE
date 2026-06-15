package vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;

    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
