
package top.wyhao.admin.system.model.vo.log;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.enums.LogStatus;
import top.wyhao.admin.system.model.enums.LoginStatusEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.time.LocalDateTime;

/**
 * 登录日志导出响应信息
 *

 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "登录日志导出响应信息")
public class LoginLogExportResult {
    @ExcelProperty(value = "用户名")
    private String username;

    @ExcelProperty(value = "登录 IP")
    private String ipAddress;

    @ExcelProperty(value = "登录地点")
    private String location;

    @ExcelProperty(value = "浏览器")
    private String browser;

    @ExcelProperty(value = "终端系统")
    private String os;

    @ExcelProperty(value = "登录状态")
    private String loginStatus;

    @ExcelProperty(value = "登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    @ExcelProperty(value = "失败原因")
    private String failureReason;
}
