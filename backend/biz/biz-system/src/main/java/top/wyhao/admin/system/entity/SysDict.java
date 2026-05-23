
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字典实体
 *

 * @since 2026/5/13
 */
@Data
@TableName(value = "sys_dict", autoResultMap = true)
public class SysDict {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典值
     */
    private String value;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 是否启用 ENABLE/DISABLE
     */
    private Boolean enabled;

    /**
     * 扩展信息(JSON)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ext;

    /**
     * 排序
     */
    private Integer sort;


    /**
     * 描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
