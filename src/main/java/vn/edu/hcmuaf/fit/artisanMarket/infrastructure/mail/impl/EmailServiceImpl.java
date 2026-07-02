package vn.edu.hcmuaf.fit.artisanMarket.infrastructure.mail.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.mail.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        log.info("======== RESET PASSWORD EMAIL ========");
        log.info("Send to: {}", toEmail);
        log.info("Reset Link: {}", resetLink);
        log.info("======================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Artisan Market] Đặt lại mật khẩu tài khoản của bạn");

            String content = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;\">" +
                    "<h2 style=\"color: #333333; text-align: center;\">Đặt Lại Mật Khẩu</h2>" +
                    "<p>Xin chào,</p>" +
                    "<p>Bạn nhận được email này vì chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản Artisan Market của bạn.</p>" +
                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                    "<a href=\"" + resetLink + "\" style=\"background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;\">Đặt Lại Mật Khẩu</a>" +
                    "</div>" +
                    "<p>Hoặc bạn có thể sao chép liên kết dưới đây vào trình duyệt của bạn:</p>" +
                    "<p style=\"word-break: break-all; color: #1a0dab;\">" + resetLink + "</p>" +
                    "<p>Liên kết này có hiệu lực trong vòng 15 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, bạn có thể bỏ qua email này.</p>" +
                    "<hr style=\"border: none; border-top: 1px solid #eeeeee; margin: 20px 0;\">" +
                    "<p style=\"color: #777777; font-size: 12px; text-align: center;\">Đây là email tự động từ hệ thống Artisan Market, vui lòng không phản hồi email này.</p>" +
                    "</div>";

            helper.setText(content, true);

            mailSender.send(message);
            log.info("Reset password email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send reset password email to {}: {}. Token/link has still been generated and printed above.", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verifyLink) {
        log.info("======== VERIFICATION EMAIL ========");
        log.info("Send to: {}", toEmail);
        log.info("Verify Link: {}", verifyLink);
        log.info("=====================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Artisan Market] Xác nhận email đăng ký tài khoản");

            String content = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;\">" +
                    "<h2 style=\"color: #333333; text-align: center;\">Xác Nhận Email</h2>" +
                    "<p>Xin chào,</p>" +
                    "<p>Cảm ơn bạn đã đăng ký tài khoản tại Artisan Market. Vui lòng bấm vào nút bên dưới để xác nhận địa chỉ email và kích hoạt tài khoản của bạn.</p>" +
                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                    "<a href=\"" + verifyLink + "\" style=\"background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;\">Xác Nhận Email</a>" +
                    "</div>" +
                    "<p>Hoặc bạn có thể sao chép liên kết dưới đây vào trình duyệt của bạn:</p>" +
                    "<p style=\"word-break: break-all; color: #1a0dab;\">" + verifyLink + "</p>" +
                    "<p>Liên kết này có hiệu lực trong vòng 24 giờ. Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email.</p>" +
                    "<hr style=\"border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                    "<p style=\"font-size: 12px; color: #999999; text-align: center;\">Artisan Market</p>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Verification email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email xác nhận thất bại: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email xác nhận, vui lòng thử lại sau");
        }
    }

    @Override
    public void sendProductApprovedEmail(String toEmail, String productName) {
        log.info("======== PRODUCT APPROVED EMAIL ========");
        log.info("Send to: {}", toEmail);
        log.info("Product: {}", productName);
        log.info("========================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Artisan Market] Sản phẩm của bạn đã được duyệt thành công");

            String content = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;\">" +
                    "<h2 style=\"color: #4CAF50; text-align: center;\">Chúc Mừng!</h2>" +
                    "<p>Xin chào,</p>" +
                    "<p>Sản phẩm <strong>" + productName + "</strong> của bạn đã được Admin phê duyệt thành công và hiện tại đã được hiển thị công khai trên sàn giao dịch Artisan Market.</p>" +
                    "<p>Bạn có thể vào trang quản lý sản phẩm của mình để theo dõi lượt xem và quản lý tồn kho của sản phẩm.</p>" +
                    "<hr style=\"border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                    "<p style=\"font-size: 12px; color: #999999; text-align: center;\">Artisan Market</p>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Product approved email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email thông báo duyệt sản phẩm thất bại: {}", e.getMessage());
        }
    }

    @Override
    public void sendProductRejectedEmail(String toEmail, String productName, String reason) {
        log.info("======== PRODUCT REJECTED EMAIL ========");
        log.info("Send to: {}", toEmail);
        log.info("Product: {}", productName);
        log.info("Reason: {}", reason);
        log.info("========================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Artisan Market] Thông báo kết quả duyệt sản phẩm");

            String content = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;\">" +
                    "<h2 style=\"color: #f44336; text-align: center;\">Sản phẩm không được duyệt</h2>" +
                    "<p>Xin chào,</p>" +
                    "<p>Chúng tôi rất tiếc phải thông báo sản phẩm <strong>" + productName + "</strong> của bạn không được phê duyệt để bán trên sàn giao dịch Artisan Market.</p>" +
                    "<p><strong>Lý do từ chối:</strong> " + reason + "</p>" +
                    "<p>Vui lòng điều chỉnh lại thông tin sản phẩm và gửi lại yêu cầu duyệt để chúng tôi có thể phê duyệt sản phẩm của bạn sớm nhất.</p>" +
                    "<hr style=\"border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                    "<p style=\"font-size: 12px; color: #999999; text-align: center;\">Artisan Market</p>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Product rejected email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email thông báo từ chối sản phẩm thất bại: {}", e.getMessage());
        }
    }
}
