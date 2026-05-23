
package top.wyhao.admin.schedule.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.schedule.model.enums.JobStatusEnum;
import top.wyhao.starter.core.validation.EnumValue;

import java.io.Serial;

/**
 * 任务查询条件

 * @since 2024/6/25 16:43
 */
@Data
@Schema(description = "任务查询条件")
public class JobQuery extends JobPageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务组
     */
    @Schema(description = "任务组", example = "tide-admin")
    private String groupName;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称", example = "定时任务1")
    private String jobName;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "1")
    @EnumValue(value = JobStatusEnum.class, message = "任务状态无效")
    private Integer jobStatus;
}
