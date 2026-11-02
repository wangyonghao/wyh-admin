package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.web.core.model.PageQuery;

@Data
@Schema(description = "任务查询")
public class JobQuery extends PageQuery {

    private String name;
    private String handlerCode;
    private Integer status;
}
