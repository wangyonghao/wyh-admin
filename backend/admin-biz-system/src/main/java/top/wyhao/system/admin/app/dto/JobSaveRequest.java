package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增或修改定时任务")
public class JobSaveRequest {

    @NotBlank(message = "任务名称不能为空")
    @Length(max = 64)
    private String name;

    @NotBlank(message = "请选择任务")
    private String handlerCode;

    @Valid
    @NotNull(message = "请配置执行时间")
    private ScheduleSpec schedule;

    private String params;
    private String remark;
    /** 创建后是否立即激活，默认否 */
    private Boolean enabled;
}
