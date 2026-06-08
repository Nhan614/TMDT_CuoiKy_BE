package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.ArtisanApplication;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.enums.ApplicationStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.repository.ArtisanApplicationRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.request.SubmitApplicationRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.response.ArtisanApplicationResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.service.ArtisanApplicationService;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.cloudinary.CloudinaryService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums.ArtisanStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.domain.repository.AuthRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtisanApplicationServiceImpl implements ArtisanApplicationService {

    private final ArtisanApplicationRepository applicationRepository;
    private final AuthRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public Map<String, String> uploadProofImage(MultipartFile file) {
        String imageUrl = cloudinaryService.uploadImage(file, "artisan-applications/proofs");
        String publicId = extractPublicId(imageUrl);

        return Map.of(
                "imageUrl", imageUrl,
                "publicId", publicId != null ? publicId : ""
        );
    }

    @Override
    @Transactional
    public ArtisanApplicationResponseDTO submitApplication(SubmitApplicationRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (user.getRole() == UserRole.ARTISAN) {
            throw new IllegalStateException("Bạn đã là nghệ nhân trên hệ thống");
        }

        if (applicationRepository.existsByUserIdAndStatus(user.getId(), ApplicationStatus.PENDING)) {
            throw new IllegalStateException("Bạn đang có đơn đăng ký đang chờ xét duyệt");
        }

        ArtisanApplication app = ArtisanApplication.builder()
                .userId(user.getId())
                .fullName(dto.fullName())
                .skill(dto.skill())
                .bio(dto.bio())
                .quote(dto.quote())
                .startedCraftingDate(dto.startedCraftingDate())
                .portfolioUrl(dto.portfolioUrl())
                .avatarUrl(dto.avatarUrl())
                .proofImageUrls(dto.proofImageUrls())
                .status(ApplicationStatus.PENDING)
                .build();

        app = applicationRepository.save(app);
        return ArtisanApplicationResponseDTO.fromEntity(app);
    }

    @Override
    public ArtisanApplicationResponseDTO getMyApplication() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        ArtisanApplication app = applicationRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa nộp đơn đăng ký nào"));

        return ArtisanApplicationResponseDTO.fromEntity(app);
    }

    @Override
    public Page<ArtisanApplicationResponseDTO> getAllApplications(ApplicationStatus status, int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<ArtisanApplication> data;
        if (status != null) {
            data = applicationRepository.findByStatus(status, pageable);
        } else {
            data = applicationRepository.findAll(pageable);
        }

        return data.map(ArtisanApplicationResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public ArtisanApplicationResponseDTO approveApplication(Long applicationId) {
        ArtisanApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đăng ký"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Đơn đăng ký không ở trạng thái chờ duyệt");
        }

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản admin"));

        User applicant = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người nộp đơn"));

        // Update application
        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedBy(admin.getId());
        app.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        // Update user role
        applicant.setRole(UserRole.ARTISAN);
        userRepository.save(applicant);

        // Create Artisan record
        Artisan artisan = Artisan.builder()
                .userId(app.getUserId())
                .name(app.getFullName())
                .tag(app.getSkill().getDisplayName())
                .image(app.getAvatarUrl())
                .rating(0.0)
                .quote(app.getQuote())
                .startedCraftingDate(app.getStartedCraftingDate())
                .totalOrders(0)
                .activeOrdersCount(0)
                .maxConcurrentOrders(5)
                .featured(false)
                .skill(app.getSkill())
                .status(ArtisanStatus.ACTIVE)
                .build();
        artisanRepository.save(artisan);

        return ArtisanApplicationResponseDTO.fromEntity(app);
    }

    @Override
    @Transactional
    public ArtisanApplicationResponseDTO rejectApplication(Long applicationId, String reason) {
        ArtisanApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đăng ký"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Đơn đăng ký không ở trạng thái chờ duyệt");
        }

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản admin"));

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRejectionReason(reason);
        app.setReviewedBy(admin.getId());
        app.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ArtisanApplicationResponseDTO.fromEntity(app);
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
            throw new RuntimeException("Không phân giải được publicId từ imageUrl");
        }
        return url;
    }
}
