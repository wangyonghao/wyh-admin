package top.wyhao.system.admin.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class OrgCreateRequest {
    @NotBlank(message = "组织名称不能为空")
    @Length(max = 30, message = "组织名称长度不能超过 {max} 个字符")
    private String name;

    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;
}
