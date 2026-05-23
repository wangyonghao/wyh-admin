
package top.wyhao.admin.system.model.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.enums.NoticeScopes;
import top.wyhao.admin.system.model.enums.NoticeStatus;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;
import top.wyhao.starter.web.excel.DictExcelProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告详情响应信息
 *

 * @since 2026/5/8
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "公告详情响应参数")
public class NoticeDetailResult {
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updateUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人", example = "李四")
    @ExcelProperty(value = "修改人", order = Integer.MAX_VALUE - 2)
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "修改时间", order = Integer.MAX_VALUE - 1)
    private LocalDateTime updateTime;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "这是公告标题")
    @ExcelProperty(value = "标题", order = 2)
    private String title;

    /**
     * 分类（取值于字典 notice_type）
     */
    @Schema(description = "分类（取值于字典 notice_type）", example = "1")
    @ExcelProperty(value = "分类", converter = top.wyhao.starter.web.excel.ExcelDictConverter.class, order = 3)
    @DictExcelProperty("notice_type")
    private String type;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "这是公告内容")
    private String content;

    /**
     * 通知范围
     */
    @Schema(description = "通知范围", example = "2")
    @ExcelProperty(value = "通知范围", converter = ExcelBaseEnumConverter.class, order = 4)
    private NoticeScopes noticeScope;

    /**
     * 通知用户
     */
    @Schema(description = "通知用户", example = "[1,2,3]")
    private List<String> noticeUsers;

    /**
     * 通知方式
     */
    @Schema(description = "通知方式", example = "[1,2]")
    private List<Integer> noticeMethods;

    /**
     * 是否定时
     */
    @Schema(description = "是否定时", example = "false")
    @ExcelProperty(value = "是否定时", order = 5)
    private Boolean isTiming;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2023-08-08 00:00:00", type = "string")
    @ExcelProperty(value = "发布时间", order = 6)
    private LocalDateTime publishTime;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶", example = "false")
    @ExcelProperty(value = "是否置顶", order = 7)
    private Boolean isTop;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "3")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 8)
    private NoticeStatus status;
}