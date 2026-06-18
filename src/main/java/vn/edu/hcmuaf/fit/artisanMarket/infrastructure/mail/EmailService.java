package vn.edu.hcmuaf.fit.artisanMarket.infrastructure.mail;

public interface EmailService {
    void sendResetPasswordEmail(String toEmail, String resetLink);
}
