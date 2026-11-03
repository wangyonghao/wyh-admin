package top.wyhao.system.admin.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.web.sensitive.Sensitive;
import top.wyhao.web.sensitive.SensitiveMethod;

import java.util.List;

/**
 * 用户响应参数
 *
 * @since 2023/2/20 21:08
 */
@Data
@Schema(description = "用户响应参数")
public class UserResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "显示名称", example = "张三")
    private String displayName;

    @Schema(description = "性别", example = "1")
    private GenderEnum gender;

    @Schema(description = "头像文件 ID", example = "1001")
    private Long avatar;

    @Schema(description = "邮箱", example = "c*******@126.com")
    @Sensitive(SensitiveMethod.EMAIL)
    private String email;

    @Schema(description = "手机号码", example = "188****8888")
    @Sensitive(SensitiveMethod.MOBILE_PHONE)
    private String phone;

    @Schema(description = "状态", example = "1")
    private String status;

    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isBuiltin;

    @Schema(description = "描述", example = "张三描述信息")
    private String description;

    @Schema(description = "部门 ID", example = "5")
    private Long departmentId;

    @Schema(description = "所属部门", example = "测试部")
    private String departmentName;

    @Schema(description = "角色名称列表", example = "测试人员")
    private List<String> roleNames;
}
