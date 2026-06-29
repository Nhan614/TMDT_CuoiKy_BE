package vn.edu.hcmuaf.fit.artisanMarket.infrastructure.cloudinary;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folder);
    void deleteImage(String publicId);

    // Phương thức mới trả về cả url và public_id
    Map<String, String> upload(MultipartFile file, String folder);
    void delete(String publicId);
}
