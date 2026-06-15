package vn.edu.hcmuaf.fit.artisanMarket.modules.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto.UserUpdateRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return UserResponseDTO.fromEntity(user);
    }

    @Override
    public UserResponseDTO updateMyProfile(String username, UserUpdateRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
                throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new RuntimeException("Vui lòng nhập mật khẩu hiện tại");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Mật khẩu hiện tại không chính xác");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        Page<User> users = userRepository.searchUsers(search, pageable);
        return users.map(UserResponseDTO::fromEntity);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return UserResponseDTO.fromEntity(user);
    }

    @Override
    public UserResponseDTO changeUserStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setStatus(status);
        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);
    }
}
