package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.AcceptCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.CreateCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.RejectCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.response.CustomOrderResponseDTO;

import java.util.Map;

public interface CustomOrderService {
    Map<String, String> uploadReferenceImage(MultipartFile file);
    
    CustomOrderResponseDTO createCustomOrder(CreateCustomOrderRequestDTO dto);
    
    Page<CustomOrderResponseDTO> getMyCustomOrders(CustomOrderStatus status, int page, int size);
    
    CustomOrderResponseDTO getMyCustomOrderDetails(Long id);
    
    CustomOrderResponseDTO cancelCustomOrder(Long id);
    
    Page<CustomOrderResponseDTO> getArtisanCustomOrders(CustomOrderStatus status, int page, int size);
    
    CustomOrderResponseDTO getArtisanCustomOrderDetails(Long id);
    
    CustomOrderResponseDTO acceptCustomOrder(Long id, AcceptCustomOrderRequestDTO dto);
    
    CustomOrderResponseDTO rejectCustomOrder(Long id, RejectCustomOrderRequestDTO dto);
}
