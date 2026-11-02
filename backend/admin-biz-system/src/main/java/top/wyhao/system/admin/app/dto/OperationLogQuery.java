package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.wyhao.mybatis.query.Query;
import top.wyhao.cmn.core.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志查询条件
 */
@Data
@Schema(description = "日志查询条件")
public class OperationLogQuery {

    @Schema(description = "业务对象类型", example = "业务对象类型")
    private String objectType;

    @Schema(description = "业务对象ID", example = "业务对象ID")
    private String objectId;

    @Schema(description = "操作IP", example = "")
    private String operatorIp;

    @Schema(description = "操作人", example = "admin")
    private String operatorName;

    @Schema(description = "日志描述", example = "新增数据")
    @Query(type = Query.Type.LIKE)
    private String operation;

    @Schema(description = "操作时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    @Size(max = 2, message = "操作时间必须是一个范围")
    @Query(type = Query.Type.BETWEEN)
    private List<LocalDateTime> createTime;

    @Schema(description = "状态", example = "1")
    private StatusEnum status;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}
