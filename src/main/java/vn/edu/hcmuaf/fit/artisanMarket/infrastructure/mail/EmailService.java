package vn.edu.hcmuaf.fit.artisanMarket.infrastructure.mail;

public interface EmailService {
    void sendResetPasswordEmail(String toEmail, String resetLink);

    // === MỚI ===
    void sendVerificationEmail(String toEmail, String verifyLink);

    void sendProductApprovedEmail(String toEmail, String productName);
    void sendProductRejectedEmail(String toEmail, String productName, String reason);
}
