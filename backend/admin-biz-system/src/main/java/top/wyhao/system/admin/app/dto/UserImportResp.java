
package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户导入结果响应参数
 *

 * @since 2024/6/18 14:37
 */
@Data
@Schema(description = "用户导入结果响应参数")
@AllArgsConstructor
@NoArgsConstructor
public class UserImportResp {



    /**
     * 总计行数
     */
    @Schema(description = "总计行数", example = "100")
    private Integer totalRows;

    /**
     * 新增行数
     */
    @Schema(description = "新增行数", example = "100")
    private Integer insertRows;

    /**
     * 修改行数
     */
    @Schema(description = "修改行数", example = "100")
    private Integer updateRows;
}
