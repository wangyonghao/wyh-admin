package top.wyhao.system.admin.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrgSwitchRequest {
    @NotNull(message = "组织 ID 不能为空")
    private Long orgId;
}
