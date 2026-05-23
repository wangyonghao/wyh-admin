package top.wyhao.admin.system.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 设备类型枚举
 *

 * @since 2026/05/08
 */
@Getter
@RequiredArgsConstructor
public enum LoginDeviceEnum {
    WEB("WEB", "网页端"),
    MOBILE("MOBILE", "应用程序端"),
    WECHAT_MINI_PROGRAM("WECHAT_MINI_PROGRAM", "微信小程序");

    @JsonValue
    @EnumValue
    private final String value;

    private final String description;
}
