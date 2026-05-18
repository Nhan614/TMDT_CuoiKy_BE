package vn.edu.hcmuaf.fit.artisanMarket.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.GoogleAuthRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.LoginRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.LoginResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.RegisterRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng nhập thành công",
                authService.login(request)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công"));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> googleLogin(@RequestBody GoogleAuthRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng nhập thành công",
                authService.googleLogin(request)
        ));
    }
}