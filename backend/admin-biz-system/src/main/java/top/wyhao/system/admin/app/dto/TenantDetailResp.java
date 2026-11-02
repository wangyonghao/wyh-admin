
package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户详情响应参数
 *


 * @since 2024/11/26 17:20
 */
@Data
@Schema(description = "租户详情响应参数")
public class TenantDetailResp extends TenantResp {



    /**
     * 租户管理员
     */
    @Schema(description = "租户管理员", example = "666")
    @ExcelProperty(value = "租户管理员", order = 11)
    private Long adminUser;
}