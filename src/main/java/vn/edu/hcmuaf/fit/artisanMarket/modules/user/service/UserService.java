package vn.edu.hcmuaf.fit.artisanMarket.modules.user.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto.UserUpdateRequestDTO;

public interface UserService {
    UserResponseDTO getMyProfile(String username);
    UserResponseDTO updateMyProfile(String username, UserUpdateRequestDTO request);
    Page<UserResponseDTO> getAllUsers(int page, int size, String search);
    UserResponseDTO getUserById(Long id);
    UserResponseDTO changeUserStatus(Long id, UserStatus status);
}
