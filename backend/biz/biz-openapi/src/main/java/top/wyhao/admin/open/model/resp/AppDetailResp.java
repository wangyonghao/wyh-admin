
package top.wyhao.admin.open.model.resp;

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
 * 应用详情响应参数
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "应用详情响应参数")
public class AppDetailResp implements Serializable {

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
    @Schema(description = "名称", example = "应用1")
    @ExcelProperty(value = "名称", order = 2)
    private String name;

    /**
     * Access Key（访问密钥）
     */
    @Schema(description = "Access Key（访问密钥）", example = "YjUyMGJjYjIxNTE0NDAxMWE1NmRiY2")
    @ExcelProperty(value = "Access Key", order = 3)
    private String accessKey;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "失效时间", order = 4)
    private LocalDateTime expireTime;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 5)
    private StatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "应用1描述信息")
    @ExcelProperty(value = "描述", order = 6)
    private String description;
}