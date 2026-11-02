package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字典选项
 */
@Data
public class Option {

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
    private Map<String, Object> ext;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 描述
     */
    private String description;

    private Long createUser;

    private LocalDateTime createTime;

    private Long updateUser;

    private LocalDateTime updateTime;
}
