
package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.cmn.core.enums.GenderEnum;

/**
 * 用户基础信息修改请求参数
 *

 * @since 2023/1/7 23:08
 */
@Data
@Schema(description = "用户基础信息修改请求参数")
public class UserBasicInfoUpdateReq {
    /**
     * 显示名称
     */
    @Schema(description = "显示名称", example = "张三")
    @NotBlank(message = "显示名称不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_NAME, message = "显示名称长度为 2-30 个字符，支持中文、字母、数字、下划线，短横线")
    private String displayName;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    @NotNull(message = "性别无效")
    private GenderEnum gender;
}
