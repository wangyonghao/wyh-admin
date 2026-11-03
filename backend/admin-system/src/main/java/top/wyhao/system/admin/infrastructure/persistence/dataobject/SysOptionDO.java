package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字典选项
 */
@Data
@TableName(value = "sys_options", autoResultMap = true)
public class SysOptionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 选项类型
     */
    private String optionType;

    /**
     * 字典值
     */
    private String value;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 是否启用
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
