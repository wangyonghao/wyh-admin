
package top.wyhao.admin.tenant.model.resp;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户响应参数
 *


 * @since 2024/11/26 17:20
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "租户响应参数")
public class TenantResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updateUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人", example = "李四")
    @ExcelProperty(value = "修改人", order = Integer.MAX_VALUE - 2)
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "修改时间", order = Integer.MAX_VALUE - 1)
    private LocalDateTime updateTime;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "Xxx租户")
    @ExcelProperty(value = "名称", order = 2)
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "T0sL6RWv0vFh")
    @ExcelProperty(value = "编码", order = 3)
    private String code;

    /**
     * 域名
     */
    @Schema(description = "域名", example = "T0sL6RWv0vFh.wyhao.top")
    @ExcelProperty(value = "域名", order = 4)
    private String domain;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", example = "2023-08-08 08:08:08")
    @ExcelProperty(value = "过期时间", order = 5)
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "租户描述")
    @ExcelProperty(value = "描述", order = 7)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 8)
    private StatusEnum status;

    /**
     * 管理员用户
     */
    @Schema(description = "管理员用户", example = "2")
    private Long adminUser;

    /**
     * 管理员用户名
     */
    @Schema(description = "管理员用户名", example = "admin")
    @ExcelProperty(value = "管理员用户名", order = 9)
    private String adminUsername;

    /**
     * 套餐 ID
     */
    @Schema(description = "套餐 ID", example = "1")
    private Long packageId;

    /**
     * 套餐名称
     */
    @Schema(description = "套餐名称", example = "初级套餐")
    @ExcelProperty(value = "套餐名称", order = 10)
    private String packageName;
}