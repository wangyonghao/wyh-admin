package top.wyhao.system.admin.app.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "已注册任务")
public class JobHandlerResponse {

    private String code;
    private String name;
    private String description;
    private boolean allowConcurrent;
}
