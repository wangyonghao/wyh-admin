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

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.GenderEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.encrypt.field.annotation.FieldEncrypt;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;
import top.wyhao.starter.excel.converter.ExcelListConverter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情响应参数
 *
 * @author Charles7c
 * @since 2023/2/20 21:11
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "用户详情响应参数")
public class UserDetailResult {
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
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime updateTime;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    @ExcelProperty(value = "用户名", order = 2)
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    @ExcelProperty(value = "昵称", order = 3)
    private String nickname;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 4)
    private StatusEnum status;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    @ExcelProperty(value = "性别", converter = ExcelBaseEnumConverter.class, order = 5)
    private GenderEnum gender;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "5")
    @ExcelProperty(value = "部门 ID", order = 6)
    private Long deptId;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门", example = "测试部")
    @ExcelProperty(value = "所属部门", order = 7)
    private String deptName;

    /**
     * 角色 ID 列表
     */
    @Schema(description = "角色 ID 列表", example = "2")
    @ExcelProperty(value = "角色 ID 列表", converter = ExcelListConverter.class, order = 8)
    private List<Long> roleIds;

    /**
     * 角色名称列表
     */
    @Schema(description = "角色名称列表", example = "测试人员")
    @ExcelProperty(value = "角色", converter = ExcelListConverter.class, order = 9)
    private List<String> roleNames;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "13811111111")
    @ExcelProperty(value = "手机号码", order = 10)
    @FieldEncrypt
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "123456789@qq.com")
    @ExcelProperty(value = "邮箱", order = 11)
    @FieldEncrypt
    private String email;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "系统内置", example = "false")
    @ExcelProperty(value = "系统内置", order = 12)
    private Boolean isBuiltin;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "张三描述信息")
    @ExcelProperty(value = "描述", order = 13)
    private String description;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "https://himg.bdimg.com/sys/portrait/item/public.1.81ac9a9e.rf1ix17UfughLQjNo7XQ_w.jpg")
    @ExcelProperty(value = "头像地址", order = 14)
    private String avatar;

    /**
     * 最后一次修改密码时间
     */
    @Schema(description = "最后一次修改密码时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime pwdResetTime;
}