package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.cloudinary.CloudinaryService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.CustomOrder;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.CustomOrderReferenceImage;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.repository.CustomOrderRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.AcceptCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.CreateCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.RejectCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.response.CustomOrderResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service.CustomOrderService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOrderServiceImpl implements CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public Map<String, String> uploadReferenceImage(MultipartFile file) {
        String imageUrl = cloudinaryService.uploadImage(file, "custom-orders/references");
        String publicId = extractPublicId(imageUrl);
        return Map.of(
            "imageUrl", imageUrl,
            "publicId", publicId != null ? publicId : ""
        );
    }

    @Override
    @Transactional
    public CustomOrderResponseDTO createCustomOrder(CreateCustomOrderRequestDTO dto) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (user.getRole() != UserRole.USER) {
            throw new IllegalStateException("Chỉ tài khoản vai trò USER mới được tạo yêu cầu gia công");
        }

        Artisan artisan = artisanRepository.findById(dto.artisanId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nghệ nhân"));

        if (artisan.getStatus() != ArtisanStatus.ACTIVE && artisan.getStatus() != ArtisanStatus.BUSY) {
            throw new IllegalStateException("Nghệ nhân hiện tại không ở trạng thái sẵn sàng nhận yêu cầu");
        }

        long pendingCount = customOrderRepository.countByUserIdAndArtisanIdAndStatus(
            user.getId(), artisan.getId(), CustomOrderStatus.PENDING
        );
        if (pendingCount >= 3) {
            throw new IllegalStateException("Bạn không thể gửi quá 3 yêu cầu đang chờ xử lý tới cùng một nghệ nhân");
        }

        CustomOrder customOrder = CustomOrder.builder()
            .user(user)
            .artisan(artisan)
            .title(dto.title())
            .description(dto.description())
            .budget(dto.budget())
            .quantity(dto.quantity())
            .deadline(dto.deadline())
            .status(CustomOrderStatus.PENDING)
            .referenceImages(new ArrayList<>())
            .build();

        if (dto.referenceImageUrls() != null) {
            for (String url : dto.referenceImageUrls()) {
                String publicId = extractPublicId(url);
                CustomOrderReferenceImage refImage = CustomOrderReferenceImage.builder()
                    .imageUrl(url)
                    .publicId(publicId)
                    .customOrder(customOrder)
                    .build();
                customOrder.getReferenceImages().add(refImage);
            }
        }

        customOrder = customOrderRepository.save(customOrder);
        return CustomOrderResponseDTO.fromEntity(customOrder);
    }

    @Override
    public Page<CustomOrderResponseDTO> getMyCustomOrders(CustomOrderStatus status, int page, int size) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CustomOrder> orders;
        if (status != null) {
            orders = customOrderRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        } else {
            orders = customOrderRepository.findByUserId(user.getId(), pageable);
        }

        return orders.map(CustomOrderResponseDTO::fromEntity);
    }

    @Override
    public CustomOrderResponseDTO getMyCustomOrderDetails(Long id) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        CustomOrder customOrder = customOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        if (!customOrder.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem yêu cầu gia công này");
        }

        return CustomOrderResponseDTO.fromEntity(customOrder);
    }

    @Override
    @Transactional
    public CustomOrderResponseDTO cancelCustomOrder(Long id) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        CustomOrder customOrder = customOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        if (!customOrder.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền hủy yêu cầu gia công này");
        }

        if (customOrder.getStatus() != CustomOrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu gia công đang ở trạng thái chờ xử lý (PENDING)");
        }

        customOrder.setStatus(CustomOrderStatus.CANCELLED);
        customOrder = customOrderRepository.save(customOrder);
        return CustomOrderResponseDTO.fromEntity(customOrder);
    }

    @Override
    public Page<CustomOrderResponseDTO> getArtisanCustomOrders(CustomOrderStatus status, int page, int size) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Artisan artisan = artisanRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản của bạn không liên kết với hồ sơ nghệ nhân"));

        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CustomOrder> orders;
        if (status != null) {
            orders = customOrderRepository.findByArtisanIdAndStatus(artisan.getId(), status, pageable);
        } else {
            orders = customOrderRepository.findByArtisanId(artisan.getId(), pageable);
        }

        return orders.map(CustomOrderResponseDTO::fromEntity);
    }

    @Override
    public CustomOrderResponseDTO getArtisanCustomOrderDetails(Long id) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Artisan artisan = artisanRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản của bạn không liên kết với hồ sơ nghệ nhân"));

        CustomOrder customOrder = customOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        if (!customOrder.getArtisan().getId().equals(artisan.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem yêu cầu gia công gửi tới nghệ nhân khác");
        }

        return CustomOrderResponseDTO.fromEntity(customOrder);
    }

    @Override
    @Transactional
    public CustomOrderResponseDTO acceptCustomOrder(Long id, AcceptCustomOrderRequestDTO dto) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Artisan artisan = artisanRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản của bạn không liên kết với hồ sơ nghệ nhân"));

        CustomOrder customOrder = customOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        if (!customOrder.getArtisan().getId().equals(artisan.getId())) {
            throw new IllegalStateException("Bạn không có quyền xử lý yêu cầu gia công gửi tới nghệ nhân khác");
        }

        if (customOrder.getStatus() != CustomOrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể chấp nhận yêu cầu gia công đang ở trạng thái chờ xử lý (PENDING)");
        }

        customOrder.setStatus(CustomOrderStatus.ACCEPTED);
        customOrder.setQuotedPrice(dto.quotedPrice());
        customOrder.setArtisanNote(dto.artisanNote());
        
        customOrder = customOrderRepository.save(customOrder);
        return CustomOrderResponseDTO.fromEntity(customOrder);
    }

    @Override
    @Transactional
    public CustomOrderResponseDTO rejectCustomOrder(Long id, RejectCustomOrderRequestDTO dto) {
        User user = userRepository.findByUsername(getCurrentUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Artisan artisan = artisanRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Tài khoản của bạn không liên kết với hồ sơ nghệ nhân"));

        CustomOrder customOrder = customOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        if (!customOrder.getArtisan().getId().equals(artisan.getId())) {
            throw new IllegalStateException("Bạn không có quyền xử lý yêu cầu gia công gửi tới nghệ nhân khác");
        }

        if (customOrder.getStatus() != CustomOrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối yêu cầu gia công đang ở trạng thái chờ xử lý (PENDING)");
        }

        customOrder.setStatus(CustomOrderStatus.REJECTED);
        customOrder.setArtisanNote(dto.artisanNote());

        customOrder = customOrderRepository.save(customOrder);
        return CustomOrderResponseDTO.fromEntity(customOrder);
    }

    private String extractPublicId(String url) {
        if (url == null) return null;
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex != -1) {
                String pathAfterUpload = url.substring(uploadIndex + 8);
                if (pathAfterUpload.startsWith("v")) {
                    int firstSlash = pathAfterUpload.indexOf('/');
                    if (firstSlash != -1) {
                        pathAfterUpload = pathAfterUpload.substring(firstSlash + 1);
                    }
                }
                int lastDot = pathAfterUpload.lastIndexOf('.');
                if (lastDot != -1) {
                    pathAfterUpload = pathAfterUpload.substring(0, lastDot);
                }
                return pathAfterUpload;
            }
        } catch (Exception e) {
            throw new RuntimeException("Không phân giải được publicId từ imageURL");
        }
        return null;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
