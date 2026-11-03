
package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 *

 * @since 2024/04/26
 */
@Data
public class Config {
    /**
     * ID
     */
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（JSON格式）
     */
    private String configValue;

    /**
     * 配置说明
     */
    private String description;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
