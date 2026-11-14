package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证链接确认结果。不返回登录 token。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邮箱注册确认结果")
public class EmailRegistrationConfirmResponse {

    @Schema(description = "成功码", example = "200")
    private String code;

    @Schema(description = "VERIFIED 刚完成注册；ALREADY_VERIFIED 链接已经确认过", example = "VERIFIED")
    private String status;
}
