package top.wyhao.admin.system.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleMemberRemoveRequest {
    /**
     * ID
     */
    @Schema(description = "ID", example = "[1,2]")
    @NotEmpty(message = "ID 不能为空")
    private List<Long> userIds;
}
