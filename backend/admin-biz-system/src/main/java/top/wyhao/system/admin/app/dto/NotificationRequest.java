package top.wyhao.system.admin.app.dto;

import cn.sticki.spel.validator.constrain.SpelFuture;
import cn.sticki.spel.validator.constrain.SpelNotEmpty;
import cn.sticki.spel.validator.constrain.SpelNotNull;
import cn.sticki.spel.validator.jakarta.SpelValid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import top.wyhao.system.admin.domain.model.NoticeScopes;
import top.wyhao.system.admin.domain.model.NoticeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告创建或修改请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SpelValid
@Schema(description = "公告创建或修改请求参数")
public class NotificationRequest {



    @Schema(description = "标题", example = "这是公告标题")
    @NotBlank(message = "标题不能为空")
    @Length(max = 150, message = "标题长度不能超过 {max} 个字符")
    private String title;

    @Schema(description = "内容", example = "这是公告内容")
    @NotBlank(message = "内容不能为空")
    private String content;

    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    @NotBlank(message = "分类不能为空")
    @Length(max = 30, message = "分类长度不能超过 {max} 个字符")
    private String type;

    @Schema(description = "通知范围", example = "2")
    @NotNull(message = "通知范围不能为空")
    private NoticeScopes noticeScope;

    @Schema(description = "通知用户", example = "[1,2,3]")
    @SpelNotEmpty(condition = "#this.noticeScope == T(top.wyhao.admin.system.notification.domain.model.NoticeScopes).USER", message = "通知用户不能为空")
    private List<String> noticeUsers;

    @Schema(description = "通知方式", example = "[1,2]")
    private List<Integer> noticeMethods;

    @Schema(description = "是否定时", example = "true")
    @NotNull(message = "是否定时不能为空")
    private Boolean isTiming;

    @Schema(description = "发布时间", example = "2023-08-08 00:00:00", type = "string")
    @SpelNotNull(condition = "#this.isTiming == true", message = "定时发布时间不能为空")
    @SpelFuture(condition = "#this.isTiming == true", message = "定时发布时间不能早于当前时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;

    @Schema(description = "状态", example = "3")
    private NoticeStatus status;
}
