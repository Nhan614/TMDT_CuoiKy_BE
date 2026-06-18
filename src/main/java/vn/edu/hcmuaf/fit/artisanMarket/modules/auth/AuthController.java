package vn.edu.hcmuaf.fit.artisanMarket.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.*;
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

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu đặt lại mật khẩu đã được gửi đến email của bạn"));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<Void>> verifyResetToken(@RequestParam String token) {
        authService.verifyResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("Mã xác thực hợp lệ"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công"));
    }
}