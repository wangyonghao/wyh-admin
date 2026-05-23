
package top.wyhao.admin.system.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 第三方账号绑定响应参数
 *

 */
@Data
@Schema(description = "第三方账号绑定响应参数")
public class UserSocialBindResp{
    /**
     * 来源
     */
    @Schema(description = "来源", example = "GITEE")
    private String source;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "码云")
    private String description;
}
