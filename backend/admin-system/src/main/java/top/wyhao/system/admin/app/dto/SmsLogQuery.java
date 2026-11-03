package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.mybatis.query.Query;
import top.wyhao.cmn.core.enums.ResultStatusEnum;

/**
 * 短信日志查询条件
 *
 * @since 2025/03/15 22:15
 */
@Data
@Schema(description = "短信日志查询条件")
public class SmsLogQuery {

    @Schema(description = "配置 ID", example = "1")
    @Query(type = Query.Type.EQ)
    private Long configId;

    @Schema(description = "手机号", example = "18888888888")
    @Query(type = Query.Type.EQ)
    private String phone;

    @Schema(description = "发送状态", example = "1")
    @Query(type = Query.Type.EQ)
    private ResultStatusEnum status;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}
