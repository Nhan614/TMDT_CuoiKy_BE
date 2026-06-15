package vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {
    private String username;
    private String token;
    private String role;
}