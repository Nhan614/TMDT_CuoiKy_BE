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
}
