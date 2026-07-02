package vn.edu.hcmuaf.fit.artisanMarket.modules.auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.domain.repository.AuthRepository;
import vn.edu.hcmuaf.fit.artisanMarket.security.CustomUserDetailsService;
import vn.edu.hcmuaf.fit.artisanMarket.security.JwtService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.service.AuthService;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.mail.EmailService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    @Value("${app.verify-email-url}")
    private String verifyEmailUrl;

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final AuthRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // check username, password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // get user details
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());

        // get user role
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Admin không cần xác thực email
        if (!user.isEmailVerified() && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác nhận trước khi đăng nhập");
        }

        String role = user.getRole().name();

        // create token with role claim
        String token = jwtService.generateToken(userDetails, role);

        return LoginResponseDTO.builder()
                .username(userDetails.getUsername())
                .token(token)
                .role(role)
                .build();
    }

    @Override
    public void register(RegisterRequestDTO request) {
        // Check username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        // Check email đã được sử dụng
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        String verificationToken = UUID.randomUUID().toString();

        // Create new user - chưa xác thực email
        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        // Save user
        userRepository.save(newUser);

        // Gửi email xác nhận
        String verifyLink = verifyEmailUrl + "?token=" + verificationToken;
        emailService.sendVerificationEmail(newUser.getEmail(), verifyLink);
    }

    @Override
    public LoginResponseDTO googleLogin(GoogleAuthRequestDTO request) {
        try {
            // Create Verifier with Client ID
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            // Verify token
            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Get user information from Google
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                // Check user in Database
                User user = userRepository
                        .findByEmail(email)
                        .orElse(User.builder()
                                .username(email)
                                .email(email)
                                .fullName(name)
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(UserRole.USER)
                                .status(UserStatus.ACTIVE)
                                .emailVerified(true)
                                .build());
                userRepository.save(user);

                // Generate JWT with role claim
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
                String role = user.getRole().name();
                String token = jwtService.generateToken(userDetails, role);

                // Response
                return LoginResponseDTO.builder()
                        .username(user.getUsername())
                        .token(token)
                        .role(role)
                        .build();
            } else {
                throw new RuntimeException("Token Google không hợp lệ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google");
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String resetLink = resetPasswordUrl + "?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
    }

    @Override
    public void verifyResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Mã xác thực không hợp lệ");
        }
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực đã hết hạn");
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO request) {
        verifyResetToken(request.getToken());

        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ"));

        if (user.isEmailVerified()) {
            return; // đã xác thực trước đó, không báo lỗi
        }

        if (user.getVerificationTokenExpiry() == null
                || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực đã hết hạn, vui lòng yêu cầu gửi lại email xác nhận");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void resendVerificationEmail(ResendVerificationRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Tài khoản đã được xác thực trước đó");
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String verifyLink = verifyEmailUrl + "?token=" + verificationToken;
        emailService.sendVerificationEmail(user.getEmail(), verifyLink);
    }
}