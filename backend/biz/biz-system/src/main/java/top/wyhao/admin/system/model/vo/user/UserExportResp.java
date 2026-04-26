/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author wyhao
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
