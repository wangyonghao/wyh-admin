package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.LogStatus;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.time.LocalDateTime;

/**
 * 操作日志导出响应参数
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "操作日志导出响应参数")
public class OperationLogExcelResult {

    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID")
    private Long id;

    @Schema(description = "操作时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "操作时间")
    private LocalDateTime createTime;

    @Schema(description = "操作人", example = "张三")
    @ExcelProperty(value = "操作人")
    private String createUserString;

    @Schema(description = "操作内容", example = "账号登录")
    @ExcelProperty(value = "操作内容")
    private String description;

    @Schema(description = "所属模块", example = "部门管理")
    @ExcelProperty(value = "所属模块")
    private String module;

    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class)
    private LogStatus status;

    @Schema(description = "操作 IP", example = "")
    @ExcelProperty(value = "操作 IP")
    private String ip;

    @Schema(description = "操作地点", example = "中国北京北京市")
    @ExcelProperty(value = "操作地点")
    private String address;

    @Schema(description = "耗时（ms）", example = "58")
    @ExcelProperty(value = "耗时（ms）")
    private Long timeTaken;

    @Schema(description = "浏览器", example = "Chrome 115.0.0.0")
    @ExcelProperty(value = "浏览器")
    private String browser;

    @Schema(description = "终端系统", example = "Windows 10")
    @ExcelProperty(value = "终端系统")
    private String os;
}
