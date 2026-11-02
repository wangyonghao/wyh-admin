
package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘-访问趋势响应参数
 *

 * @since 2023/9/9 20:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘-访问趋势响应参数")
public class DashboardAccessTrendResp {



    /**
     * 日期
     */
    @Schema(description = "日期", example = "2023-08-08")
    private String date;

    /**
     * 浏览量（PV）
     */
    @Schema(description = "浏览量（PV）", example = "1000")
    private Long pvCount;

    /**
     * IP 数
     */
    @Schema(description = "IP 数", example = "500")
    private Long ipCount;
}
