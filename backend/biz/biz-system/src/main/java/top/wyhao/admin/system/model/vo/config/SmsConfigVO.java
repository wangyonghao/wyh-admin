
package top.wyhao.admin.system.model.vo.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信配置
 *

 * @since 2024/04/26
 */
@Data
@Schema(description = "短信配置")
public class SmsConfigVO {

    /**
     * 短信服务商
     */
    @Schema(description = "短信服务商：aliyun, tencent", example = "aliyun")
    private String provider;

    /**
     * AccessKey（敏感字段）
     */
    @Schema(description = "AccessKey", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String accessKey;

    /**
     * SecretKey（敏感字段）
     */
    @Schema(description = "SecretKey", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String secretKey;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名", example = "")
    private String signName;
}
