package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.enums;

import lombok.Getter;

@Getter
public enum ArtisanSkill {

    AMIGURUMI("Amigurumi"),
    DAN_MOC("Đan móc len"),
    THIET_KE_HOA_TIET("Thiết kế họa tiết"),
    THEU_TAY("Thêu tay thủ công"),
    GOM_SU("Gốm sứ nghệ thuật");

    private final String displayName;

    ArtisanSkill(String displayName) {
        this.displayName = displayName;
    }
}