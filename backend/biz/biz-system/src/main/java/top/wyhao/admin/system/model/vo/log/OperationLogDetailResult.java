
package top.wyhao.admin.system.model.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志详情响应参数
 *

 * @since 2023/1/18 20:19
 */
@Data
@Schema(description = "日志详情响应参数")
public class OperationLogDetailResult {
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 业务对象类型 如：user、sms、login、bug、task、project
     */
    @Schema(description = "业务对象类型", example = "user")
    private String objectType;

    /**
     * 业务对象ID
     */
    @Schema(description = "业务对象ID", example = "1")
    private Long objectId;

    /**
     * 操作类型 如：login、send、create、update、delete、logout
     */
    @Schema(description = "操作类型", example = "create")
    private String operation;

    /**
     * 操作者ID
     */
    @Schema(description = "操作者ID", example = "1")
    private Long operatorId;

    /**
     * 操作者名称
     */
    @Schema(description = "操作者名称", example = "张三")
    private String operatorName;

    /**
     * 操作者IP
     */
    @Schema(description = "操作者IP", example = "192.168.1.1")
    private String operatorIp;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime operateTime;

    /**
     * 状态 success / fail
     */
    @Schema(description = "状态", example = "success")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "操作成功")
    private String remark;

    /**
     * 额外信息 JSON格式
     */
    @Schema(description = "额外信息", example = "{\"key\": \"value\"}")
    private String extra;
}
