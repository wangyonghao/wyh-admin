package top.wyhao.system.admin.domain.enums;

import top.wyhao.cmn.core.enums.BaseEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件状态枚举
 *
 * @author wyh
 * @since 2026/09/16
 */
@Getter
@AllArgsConstructor
public enum FileStatus implements BaseEnum {

    /**
     * 正常
     */
    ACTIVE(1, "正常"),

    /**
     * 已删除
     */
    DELETED(2, "已删除"),

    /**
     * 已清除（物理删除标记）
     */
    PURGED(3, "已清除");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 描述
     */
    @JsonValue
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 状态值
     * @return 文件状态枚举
     */
    public static FileStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (FileStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
