package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums;

import lombok.Getter;

@Getter
public enum ArtisanStatus {

    ACTIVE("Đang hoạt động"),
    BUSY("Tạm bận"),
    PAUSED("Tạm nghỉ"),
    BANNED("Đã khóa tài khoản");

    private final String description;

    ArtisanStatus(String description) {
        this.description = description;
    }
}