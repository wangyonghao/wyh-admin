package top.wyhao.settings.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 站点配置。
 */
@Data
@Schema(description = "站点配置")
public class SiteConfigVO {

    @Schema(description = "站点名称", example = "WYH Admin")
    private String siteName;

    @Schema(description = "站点Logo URL", example = "")
    private String siteLogo;

    @Schema(description = "版权信息", example = "Copyright © 2024 WYH Admin")
    private String siteCopyright;

    @Schema(description = "ICP备案号", example = "")
    private String siteIcp;
}
