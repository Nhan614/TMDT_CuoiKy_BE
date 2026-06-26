package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderPaymentStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.repository.CustomOrderRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.AcceptCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.CreateCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.RejectCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.response.CustomOrderResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service.CustomOrderService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.config.VNPayConfig;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.util.VNPayUtil;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomOrderServiceImpl implements CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final CloudinaryService cloudinaryService;
    private final VNPayUtil vnpayUtil;
    private final VNPayConfig vnpayConfig;

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

        if (customOrder.getStatus() != CustomOrderStatus.PENDING
                && customOrder.getStatus() != CustomOrderStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Chỉ có thể hủy yêu cầu gia công ở trạng thái chờ xử lý (PENDING) hoặc đã chấp nhận chưa thanh toán (ACCEPTED)");
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

    // ── Payment Methods ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public String confirmAndPay(Long customOrderId, HttpServletRequest request) {
        log.info("Khách hàng xác nhận thanh toán cho đơn gia công ID: {}", customOrderId);

        CustomOrder customOrder = customOrderRepository.findById(customOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        // Chỉ cho phép thanh toán khi đơn ở trạng thái ACCEPTED
        if (customOrder.getStatus() != CustomOrderStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Chỉ có thể thanh toán đơn gia công ở trạng thái đã được thợ chấp nhận (ACCEPTED)");
        }

        // quotedPrice bắt buộc phải có trước khi tạo link VNPay
        if (customOrder.getQuotedPrice() == null) {
            throw new IllegalStateException("Thợ chưa đưa ra giá báo, không thể thanh toán");
        }

        // Kiểm tra đúng chủ đơn
        String username = getCurrentUsername();
        if (!customOrder.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("Bạn không có quyền thanh toán đơn gia công này");
        }

        // Chuyển sang PAYMENT_PENDING
        customOrder.setStatus(CustomOrderStatus.PAYMENT_PENDING);
        customOrderRepository.save(customOrder);

        // Dùng prefix CO- để phân biệt với đơn hàng thường trong callback VNPay
        String txnRef = "CO-" + customOrderId;
        String orderInfo = "Thanh toan don gia cong #" + customOrderId;
        String paymentUrl = vnpayUtil.createPaymentUrl(txnRef, customOrder.getQuotedPrice(), orderInfo, request);
        log.info("Đã tạo link thanh toán VNPay cho đơn gia công ID: {}", customOrderId);
        return paymentUrl;
    }

    @Override
    @Transactional
    public String processPaymentCallback(Map<String, String> queryParams) {
        String txnRef = queryParams.get("vnp_TxnRef");
        Long customOrderId = Long.parseLong(txnRef.substring(3)); // Strip "CO-"
        log.info("Xử lý VNPay callback cho Custom Order ID: {}", customOrderId);

        CustomOrder customOrder = customOrderRepository.findById(customOrderId)
                .orElse(null);

        if (customOrder == null) {
            log.error("Không tìm thấy đơn gia công với ID: {}", customOrderId);
            return vnpayConfig.getFrontendReturnUrl() + "?status=failed&message=CustomOrderNotFound";
        }

        // Idempotency: Bỏ qua nếu đã xử lý thành công trước đó
        if (customOrder.getPaymentStatus() == CustomOrderPaymentStatus.PAID) {
            log.info("Đơn gia công {} đã thanh toán thành công trước đó, bỏ qua cập nhật", customOrderId);
            return vnpayConfig.getFrontendReturnUrl() + "?type=custom-order&id=" + customOrderId + "&status=success";
        }

        String responseCode = queryParams.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            log.info("Thanh toán đơn gia công {} thành công", customOrderId);
            customOrder.setStatus(CustomOrderStatus.IN_PROGRESS);
            customOrder.setPaymentStatus(CustomOrderPaymentStatus.PAID);
            customOrder.setPaymentTransactionId(queryParams.get("vnp_TransactionNo"));
            customOrder.setPaymentAt(LocalDateTime.now());
            customOrderRepository.save(customOrder);
            return vnpayConfig.getFrontendReturnUrl() + "?type=custom-order&id=" + customOrderId + "&status=success";
        } else {
            // Thanh toán thất bại — quay lại ACCEPTED để khách thử lại
            log.warn("Thanh toán đơn gia công {} thất bại, ResponseCode: {}", customOrderId, responseCode);
            customOrder.setStatus(CustomOrderStatus.ACCEPTED);
            customOrder.setPaymentStatus(CustomOrderPaymentStatus.FAILED);
            customOrderRepository.save(customOrder);
            return vnpayConfig.getFrontendReturnUrl() + "?type=custom-order&id=" + customOrderId + "&status=failed&responseCode=" + responseCode;
        }
    }

    @Override
    @Transactional
    public CustomOrderResponseDTO completeCustomOrder(Long customOrderId) {
        log.info("Thợ đánh dấu hoàn thành đơn gia công ID: {}", customOrderId);

        User user = userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Artisan artisan = artisanRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản của bạn không liên kết với hồ sơ nghệ nhân"));

        CustomOrder customOrder = customOrderRepository.findById(customOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia công"));

        // Chỉ thợ được giao đơn mới có thể hoàn thành
        if (!customOrder.getArtisan().getId().equals(artisan.getId())) {
            throw new IllegalStateException("Bạn không có quyền hoàn thành đơn gia công của nghệ nhân khác");
        }

        // Chỉ cho phép hoàn thành khi đang IN_PROGRESS
        if (customOrder.getStatus() != CustomOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Chỉ có thể đánh dấu hoàn thành đơn gia công đang ở trạng thái thực hiện (IN_PROGRESS)");
        }

        customOrder.setStatus(CustomOrderStatus.COMPLETED);
        customOrder = customOrderRepository.save(customOrder);
        log.info("Đơn gia công {} đã được đánh dấu COMPLETED", customOrderId);
        return CustomOrderResponseDTO.fromEntity(customOrder);
    }
}
