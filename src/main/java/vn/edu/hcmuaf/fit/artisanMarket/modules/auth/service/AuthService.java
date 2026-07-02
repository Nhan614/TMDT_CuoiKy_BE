package vn.edu.hcmuaf.fit.artisanMarket.modules.auth.service;

import org.springframework.web.bind.annotation.RequestBody;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.*;

public interface AuthService {
    LoginResponseDTO login(@RequestBody LoginRequestDTO request);
    void register(@RequestBody RegisterRequestDTO request);
    LoginResponseDTO googleLogin(@RequestBody GoogleAuthRequestDTO request);
    void forgotPassword(ForgotPasswordRequestDTO request);
    void resetPassword(ResetPasswordRequestDTO request);
    void verifyResetToken(String token);

    // === MỚI ===
    void verifyEmail(String token);
    void resendVerificationEmail(ResendVerificationRequestDTO request);
}