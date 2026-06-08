package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.enums.ApplicationStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.request.SubmitApplicationRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.response.ArtisanApplicationResponseDTO;

import java.util.Map;

public interface ArtisanApplicationService {
    Map<String, String> uploadProofImage(MultipartFile file);
    ArtisanApplicationResponseDTO submitApplication(SubmitApplicationRequestDTO dto);
    ArtisanApplicationResponseDTO getMyApplication();
    Page<ArtisanApplicationResponseDTO> getAllApplications(ApplicationStatus status, int page, int size);
    ArtisanApplicationResponseDTO approveApplication(Long applicationId);
    ArtisanApplicationResponseDTO rejectApplication(Long applicationId, String reason);
}
