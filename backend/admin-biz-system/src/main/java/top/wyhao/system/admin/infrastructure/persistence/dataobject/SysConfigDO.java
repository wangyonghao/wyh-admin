
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 *

 * @since 2024/04/26
 */
@Data
@TableName(value = "sys_config", autoResultMap = true)
public class SysConfigDO {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
