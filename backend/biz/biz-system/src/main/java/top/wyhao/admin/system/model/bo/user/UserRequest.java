
package top.wyhao.admin.system.model.bo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wyhao.starter.core.constant.RegexConstants;
import top.wyhao.starter.core.enums.GenderEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.validation.Mobile;

import java.util.List;

/**
 * 用户创建或修改请求参数
 *

 * @since 2023/2/20 21:03
 */
@Data
@Schema(description = "用户创建或修改请求参数")
public class UserRequest {
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "用户名不能为空", groups = Create.class)
    @Null(message = "不能修改用户名", groups = Update.class)
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String username;
    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    @NotBlank(message = "昵称不能为空", groups = Create.class)
    @Pattern(regexp = RegexConstants.GENERAL_NAME, message = "昵称长度为 2-30 个字符，支持中文、字母、数字、下划线，短横线")
    private String nickname;
    /**
     * 密码
     */
    @Schema(description = "密码", example = "RSA 公钥加密的密码")
    @NotBlank(message = "密码不能为空", groups = Create.class)
    private String password;
    /**
     * 所属部门
     */
    @Schema(description = "所属部门", example = "5")
    @NotNull(message = "所属部门不能为空", groups = Create.class)
    private Long deptId;
    /**
     * 所属角色
     */
    @Schema(description = "所属角色", example = "2")
    @NotEmpty(message = "所属角色不能为空", groups = Create.class)
    private List<Long> roleIds;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "123456789@qq.com")
    @Length(max = 255, message = "邮箱长度不能超过 {max} 个字符")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13811111111")
    @Mobile
    private String phone;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    private GenderEnum gender;

    public interface Create extends Default {
    }

    public interface Update extends Default {
    }

    /**
     * 描述
     */
    @Schema(description = "描述", example = "张三描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private StatusEnum status;
}