
package top.wyhao.admin.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 头像上传响应参数
 *

 * @since 2023/1/2 16:29
 */
@Data
@Builder
@Schema(description = "头像上传响应参数")
public class AvatarResult {

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "https://himg.bdimg.com/sys/portrait/item/public.1.81ac9a9e.rf1ix17UfughLQjNo7XQ_w.jpg")
    private String avatar;
}
