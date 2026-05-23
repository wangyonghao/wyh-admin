
package top.wyhao.admin.system.model.vo.user;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentStyle;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import cn.idev.excel.annotation.write.style.HeadStyle;
import cn.idev.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户导出响应参数
 *

 * @since 2024/04/24
 */
@Data
@HeadRowHeight(20)
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@Schema(description = "用户导出响应参数")
public class UserExportResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名")
    @ColumnWidth(20)
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @ExcelProperty(value = "昵称")
    @ColumnWidth(20)
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 性别
     */
    @ExcelProperty(value = "性别")
    @ColumnWidth(10)
    @Schema(description = "性别", example = "男")
    private String gender;

    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱")
    @ColumnWidth(30)
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号码")
    @ColumnWidth(20)
    @Schema(description = "手机号码", example = "18888888888")
    private String phone;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    @ColumnWidth(10)
    @Schema(description = "状态", example = "启用")
    private String status;

    /**
     * 所属部门
     */
    @ExcelProperty(value = "所属部门")
    @ColumnWidth(30)
    @Schema(description = "所属部门", example = "测试部")
    private String deptName;

    /**
     * 角色
     */
    @ExcelProperty(value = "角色")
    @ColumnWidth(30)
    @Schema(description = "角色", example = "测试人员")
    private String roleNames;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    @ColumnWidth(40)
    @Schema(description = "描述", example = "张三描述信息")
    private String description;
}
