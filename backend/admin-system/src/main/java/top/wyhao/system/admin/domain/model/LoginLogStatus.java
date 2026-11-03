package top.wyhao.system.admin.domain.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 登录状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum LoginLogStatus {

    SUCCESS("SUCCESS", "成功"),
    FAILURE("FAILURE", "失败");

    @JsonValue
    @EnumValue
    private final String value;

    private final String description;
}
