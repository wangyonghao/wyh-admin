package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码修改请求参数
 */
@Data
@Schema(description = "密码修改请求参数")
public class ProfilePasswordUpdateRequest {

    @Schema(description = "当前密码", example = "RSA 公钥加密的当前密码")
    private String oldPassword;

    @Schema(description = "新密码", example = "RSA 公钥加密的新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
