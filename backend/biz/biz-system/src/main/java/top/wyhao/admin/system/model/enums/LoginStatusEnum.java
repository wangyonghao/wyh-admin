package top.wyhao.admin.system.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 登录状态枚举
 *

 * @since 2026/05/08
 */
@Getter
@RequiredArgsConstructor
public enum LoginStatusEnum {

    /**
     * 登录成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 登录失败
     */
    FAILURE("FAILURE", "失败");

    @JsonValue
    @EnumValue
    private final String value;

    private final String description;
}
