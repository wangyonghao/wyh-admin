
package top.wyhao.admin.system.model.bo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户密码修改请求参数
 *

 * @since 2023/1/9 23:28
 */
@Data
@Schema(description = "用户密码修改请求参数")
public class UserPasswordUpdateReq implements Serializable {
    /**
     * 当前密码
     */
    @Schema(description = "当前密码", example = "RSA 公钥加密的当前密码")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "RSA 公钥加密的新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
