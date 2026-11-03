package top.wyhao.system.admin.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class OrgApplyRequest {
    @NotBlank(message = "组织编码不能为空")
    @Length(max = 30)
    private String code;

    @Length(max = 200)
    private String message;
}
