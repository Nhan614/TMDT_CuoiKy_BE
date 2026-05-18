package vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
}