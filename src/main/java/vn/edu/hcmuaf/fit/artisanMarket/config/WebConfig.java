package vn.edu.hcmuaf.fit.artisanMarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserStatus;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Cấu hình chuyển đổi String từ URL thành Enum UserStatus mượt mà
        registry.addConverter(new Converter<String, UserStatus>() {
            @Override
            public UserStatus convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                String val = source.toUpperCase().trim();

                // Ánh xạ nếu truyền BANNED thì tự động hiểu là LOCKED
                if ("BANNED".equals(val)) {
                    return UserStatus.LOCKED;
                }

                try {
                    return UserStatus.valueOf(val);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ: " + source);
                }
            }
        });
    }
}