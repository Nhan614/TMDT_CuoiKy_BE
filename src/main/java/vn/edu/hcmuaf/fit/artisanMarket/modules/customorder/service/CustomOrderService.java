package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service;

import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * Khách hàng xác nhận báo giá và khởi tạo thanh toán VNPay.
     * Chuyển trạng thái đơn từ ACCEPTED → PAYMENT_PENDING.
     * Trả về URL thanh toán VNPay.
     */
    String confirmAndPay(Long customOrderId, HttpServletRequest request);

    /**
     * Xử lý callback từ VNPay sau khi khách hàng hoàn tất thanh toán.
     * Cập nhật trạng thái sang IN_PROGRESS (thành công) hoặc quay lại ACCEPTED (thất bại).
     * Trả về URL redirect về frontend.
     */
    String processPaymentCallback(Map<String, String> queryParams);

    /**
     * Thợ thủ công đánh dấu đơn gia công đã hoàn thành.
     * Chuyển trạng thái từ IN_PROGRESS → COMPLETED.
     */
    CustomOrderResponseDTO completeCustomOrder(Long customOrderId);
}
