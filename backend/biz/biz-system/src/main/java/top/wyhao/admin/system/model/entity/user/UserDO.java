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

package top.wyhao.admin.system.model.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import top.wyhao.starter.encrypt.field.annotation.FieldEncrypt;
import top.wyhao.starter.web.core.annotation.DictModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author Charles7c
 * @since 2022/12/21 20:42
 */
@Data
@DictModel(labelKey = "nickname", extraKeys = {"username"})
@TableName("sys_user")
public class UserDO {
    @TableId
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 密码
     */
    private String password;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 邮箱
     */
    @FieldEncrypt
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String email;

    /**
     * 手机号码
     */
    @FieldEncrypt
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;
    /**
     * 头像地址
     */
    private String avatar;
    /**
     * 描述
     */
    private String description;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 是否为系统内置数据
     */
    private Boolean isBuiltin;
    /**
     * 最近改密时间
     */
    private LocalDateTime pwdUpdateTime;
    /**
     * 密码过期日
     */
    private LocalDate pwdExpireDate;
    /**
     * 部门 ID
     */
    private Long deptId;
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
