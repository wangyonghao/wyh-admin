
package top.wyhao.system.open.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用密钥响应参数


 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用密钥响应参数")
public class AppSecretResp {



    /**
     * Access Key（访问密钥）
     */
    @Schema(description = "Access Key（访问密钥）", example = "YjUyMGJjYjIxNTE0NDAxMWE1NmRiY2")
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @Schema(description = "Secret Key（私有密钥）", example = "MDI2YzQ3YTU1NGEyNDM1ZWIwNTU5NmNjNmZjM2M2Nzg=")
    private String secretKey;
}
