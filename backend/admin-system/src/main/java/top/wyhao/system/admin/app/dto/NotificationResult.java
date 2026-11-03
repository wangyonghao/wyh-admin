package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.NoticeScopes;
import top.wyhao.system.admin.domain.model.NoticeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告响应参数
 *
 * @since 2023/8/20 10:55
 */
@Data
@Schema(description = "公告响应参数")
public class NotificationResult {



    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    @JsonIgnore
    private Long createUser;

    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    @Schema(description = "标题", example = "这是公告标题")
    private String title;

    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    private String type;

    @Schema(description = "通知范围(1.所有人 2.指定用户)", example = "1")
    private NoticeScopes noticeScope;

    @Schema(description = "通知方式", example = "[1,2]")
    private List<Integer> noticeMethods;

    @Schema(description = "是否定时", example = "false")
    private Boolean isTiming;

    @Schema(description = "发布时间", example = "2023-08-08 00:00:00", type = "string")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;

    @Schema(description = "状态", example = "3")
    private NoticeStatus status;

    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;
}
