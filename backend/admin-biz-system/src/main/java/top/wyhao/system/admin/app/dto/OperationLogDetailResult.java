package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志详情响应参数
 */
@Data
@Schema(description = "日志详情响应参数")
public class OperationLogDetailResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "业务对象类型", example = "user")
    private String objectType;

    @Schema(description = "业务对象ID", example = "1")
    private Long objectId;

    @Schema(description = "操作类型", example = "create")
    private String operation;

    @Schema(description = "操作者ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作者名称", example = "张三")
    private String operatorName;

    @Schema(description = "操作者IP", example = "192.168.1.1")
    private String operatorIp;

    @Schema(description = "操作时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime operateTime;

    @Schema(description = "状态", example = "success")
    private String status;

    @Schema(description = "备注", example = "操作成功")
    private String remark;

    @Schema(description = "额外信息", example = "{\"key\": \"value\"}")
    private String extra;
}
