package vn.edu.hcmuaf.fit.artisanMarket.infrastructure.cloudinary;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folder);
    void deleteImage(String publicId);
}
