package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wyhao.cmn.core.constant.RegexConstants;

import java.util.Map;

/**
 * 选项创建或修改请求参数
 */
@Data
@Schema(description = "选项创建或修改请求参数")
public class OptionRequest {

    @Schema(description = "选项类型", example = "notice_type")
    @NotBlank(message = "选项类型不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_CODE, message = "选项类型长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String optionType;

    @Schema(description = "选项值", example = "1")
    @NotBlank(message = "选项值不能为空")
    @Length(max = 255, message = "选项值长度不能超过 {max} 个字符")
    private String value;

    @Schema(description = "选项标签", example = "产品新闻")
    @NotBlank(message = "选项标签不能为空")
    @Length(max = 255, message = "选项标签长度不能超过 {max} 个字符")
    private String label;

    @Schema(description = "扩展信息", example = "{\"color\": \"primary\"}")
    private Map<String, Object> extra;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "描述", example = "公告类型描述信息")
    @Length(max = 500, message = "描述长度不能超过 {max} 个字符")
    private String description;
}
