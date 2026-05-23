
package top.wyhao.admin.system.model.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录配置
 *

 * @since 2024/04/26
 */
@Data
@Schema(description = "登录配置")
public class LoginConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否开启验证码
     */
    @Schema(description = "是否开启验证码", example = "true")
    private Boolean captchaEnabled;

    /**
     * 验证码类型
     */
    @Schema(description = "验证码类型：graphic-图形验证码，behavior-行为验证码", example = "graphic")
    private String captchaType;

    /**
     * 最大重试次数
     */
    @Schema(description = "登录最大重试次数", example = "5")
    private Integer maxRetry;

    /**
     * 锁定时间（分钟）
     */
    @Schema(description = "登录锁定时间（分钟）", example = "30")
    private Integer lockTime;
}
