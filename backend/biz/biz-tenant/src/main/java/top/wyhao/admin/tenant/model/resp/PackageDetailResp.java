
package top.wyhao.admin.tenant.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * 套餐详情响应参数
 *


 * @since 2024/11/26 11:25
 */
@Data
@Schema(description = "套餐详情响应参数")
public class PackageDetailResp extends PackageResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联的菜单 ID 列表
     */
    @Schema(description = "关联的菜单 ID 列表", example = "[1000, 1010, 1011]")
    private List<Long> menuIds;
}