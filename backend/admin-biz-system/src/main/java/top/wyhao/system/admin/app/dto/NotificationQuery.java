package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公告查询条件
 *
 * @since 2023/8/20 10:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告查询条件")
public class NotificationQuery {

    @Schema(description = "标题", example = "这是公告标题")
    private String title;

    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    private String type;

    @Schema(hidden = true)
    private Long userId;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}
