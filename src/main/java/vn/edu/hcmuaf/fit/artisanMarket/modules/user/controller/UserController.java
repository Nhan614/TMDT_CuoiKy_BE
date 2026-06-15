package vn.edu.hcmuaf.fit.artisanMarket.modules.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.dto.UserUpdateRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponseDTO response = userService.getMyProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân thành công", response));
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMyProfile(@RequestBody UserUpdateRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponseDTO response = userService.updateMyProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", response));
    }

    @GetMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<UserResponseDTO> users = userService.getAllUsers(page, size, search);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", users));
    }

    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        UserResponseDTO response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", response));
    }

    @PatchMapping("/api/admin/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changeUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        UserResponseDTO response = userService.changeUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Thay đổi trạng thái tài khoản thành công", response));
    }
}
