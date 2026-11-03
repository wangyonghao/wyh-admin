package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.core.enums.ResultStatusEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.time.LocalDateTime;

/**
 * 短信日志响应参数
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "短信日志响应参数")
public class SmsLogResult {

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

    @Schema(description = "配置 ID")
    @ExcelProperty(value = "配置 ID")
    private Long configId;

    @Schema(description = "手机号", example = "18888888888")
    @ExcelProperty(value = "手机号")
    private String phone;

    @Schema(description = "参数配置")
    @ExcelProperty(value = "参数配置")
    private String params;

    @Schema(description = "发送状态", example = "1")
    @ExcelProperty(value = "发送状态", converter = ExcelBaseEnumConverter.class)
    private ResultStatusEnum status;

    @Schema(description = "返回数据")
    @ExcelProperty(value = "返回数据")
    private String resMsg;
}
