
package top.wyhao.admin.system.model.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 站点配置
 *

 * @since 2024/04/26
 */
@Data
@Schema(description = "站点配置")
public class SiteConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 站点名称
     */
    @Schema(description = "站点名称", example = "WYH Admin")
    private String siteName;

    /**
     * 站点Logo
     */
    @Schema(description = "站点Logo URL", example = "")
    private String siteLogo;

    /**
     * 版权信息
     */
    @Schema(description = "版权信息", example = "Copyright © 2024 WYH Admin")
    private String siteCopyright;

    /**
     * ICP备案号
     */
    @Schema(description = "ICP备案号", example = "")
    private String siteIcp;
}
