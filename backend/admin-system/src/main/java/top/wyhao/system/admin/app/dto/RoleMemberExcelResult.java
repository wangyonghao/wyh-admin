package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentStyle;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import cn.idev.excel.annotation.write.style.HeadStyle;
import cn.idev.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户导出响应参数
 */
@Data
@HeadRowHeight(20)
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@Schema(description = "用户导出响应参数")
public class RoleMemberExcelResult {

    @ExcelProperty(value = "用户名")
    @ColumnWidth(20)
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @ExcelProperty(value = "显示名称")
    @ColumnWidth(20)
    @Schema(description = "显示名称", example = "张三")
    private String displayName;

    @ExcelProperty(value = "性别")
    @ColumnWidth(10)
    @Schema(description = "性别", example = "男")
    private String gender;

    @ExcelProperty(value = "邮箱")
    @ColumnWidth(30)
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @ExcelProperty(value = "手机号码")
    @ColumnWidth(20)
    @Schema(description = "手机号码", example = "18888888888")
    private String phone;

    @ExcelProperty(value = "状态")
    @ColumnWidth(10)
    @Schema(description = "状态", example = "启用")
    private String status;

    @ExcelProperty(value = "所属部门")
    @ColumnWidth(30)
    @Schema(description = "所属部门", example = "测试部")
    private String departmentName;

    @ExcelProperty(value = "角色")
    @ColumnWidth(30)
    @Schema(description = "角色", example = "测试人员")
    private String roleNames;

    @ExcelProperty(value = "描述")
    @ColumnWidth(40)
    @Schema(description = "描述", example = "张三描述信息")
    private String description;
}
