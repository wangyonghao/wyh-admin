package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "头像上传响应参数")
public class ProfileAvatarResult {

    @Schema(description = "头像文件 ID", example = "1001")
    private Long avatar;
}
