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
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.GoogleAuthRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.LoginRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.LoginResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.dto.RegisterRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.domain.repository.AuthRepository;
import vn.edu.hcmuaf.fit.artisanMarket.security.CustomUserDetailsService;
import vn.edu.hcmuaf.fit.artisanMarket.security.JwtService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.service.AuthService;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final AuthRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Create new user
        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        // Save user
        userRepository.save(newUser);
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
}