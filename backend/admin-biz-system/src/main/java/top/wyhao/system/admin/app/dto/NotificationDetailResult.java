package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.system.admin.domain.model.NoticeScopes;
import top.wyhao.system.admin.domain.model.NoticeStatus;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;
import top.wyhao.web.excel.OptionExcelProperty;
import top.wyhao.web.excel.ExcelOptionConverter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告详情响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
@Schema(description = "公告详情响应参数")
public class NotificationDetailResult {

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

    @JsonIgnore
    private Long updateUser;

    @Schema(description = "修改人", example = "李四")
    @ExcelProperty(value = "修改人", order = Integer.MAX_VALUE - 2)
    private String updateUserString;

    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "修改时间", order = Integer.MAX_VALUE - 1)
    private LocalDateTime updateTime;

    @Schema(description = "标题", example = "这是公告标题")
    @ExcelProperty(value = "标题", order = 2)
    private String title;

    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    @ExcelProperty(value = "分类", converter = ExcelOptionConverter.class, order = 3)
    @OptionExcelProperty("notice_type")
    private String type;

    @Schema(description = "内容", example = "这是公告内容")
    private String content;

    @Schema(description = "通知范围", example = "2")
    @ExcelProperty(value = "通知范围", converter = ExcelBaseEnumConverter.class, order = 4)
    private NoticeScopes noticeScope;

    @Schema(description = "通知用户", example = "[1,2,3]")
    private List<String> noticeUsers;

    @Schema(description = "通知方式", example = "[1,2]")
    private List<Integer> noticeMethods;

    @Schema(description = "是否定时", example = "false")
    @ExcelProperty(value = "是否定时", order = 5)
    private Boolean isTiming;

    @Schema(description = "发布时间", example = "2023-08-08 00:00:00", type = "string")
    @ExcelProperty(value = "发布时间", order = 6)
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶", example = "false")
    @ExcelProperty(value = "是否置顶", order = 7)
    private Boolean isTop;

    @Schema(description = "状态", example = "3")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 8)
    private NoticeStatus status;
}
