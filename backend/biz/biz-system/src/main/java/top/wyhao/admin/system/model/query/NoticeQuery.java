
package top.wyhao.admin.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.web.core.model.SortQuery;

import java.io.Serial;
import java.io.Serializable;

/**
 * 公告查询条件
 *

 * @since 2023/8/20 10:55
 */
@Data
@Schema(description = "公告查询条件")
public class NoticeQuery extends SortQuery {

    /**
     * 标题
     */
    @Schema(description = "标题", example = "这是公告标题")
    private String title;

    /**
     * 分类（取值于字典 notice_type）
     */
    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    private String type;

    /**
     * 用户 ID
     */
    @Schema(hidden = true)
    private Long userId;
}