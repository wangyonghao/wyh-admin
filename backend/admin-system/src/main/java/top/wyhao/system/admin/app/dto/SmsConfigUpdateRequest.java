package top.wyhao.system.admin.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 短信配置更新请求。
 */
@Data
@Schema(description = "短信配置")
public class SmsConfigUpdateRequest {

    @Schema(description = "短信服务商：aliyun, tencent", example = "aliyun")
    private String provider;

    @Schema(description = "AccessKey", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String accessKey;

    @Schema(description = "SecretKey", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String secretKey;

    @Schema(description = "短信签名", example = "")
    private String signName;
}
