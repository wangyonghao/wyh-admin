package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "修改任务状态")
public class JobStatusRequest {

    @NotNull
    @Schema(description = "0 停止 1 激活")
    private Integer status;
}
