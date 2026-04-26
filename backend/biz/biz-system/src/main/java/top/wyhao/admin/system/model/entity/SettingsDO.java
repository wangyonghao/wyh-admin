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

package top.wyhao.admin.system.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置表
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:20
 */
@Data
@TableName("sys_settings")
public class ConfigDO {

    @TableId
    private Long id;

    /**
     * 类别
     */
    private String category;

    /**
     * 配置分组: site, register, login, email, sms, storage, push, oauth, payment, security
     */
    private String name;

    /**
     * 配置键，如 site_name, login_captcha_type
     */
    private String config_key;

    /**
     * 配置值。简单类型存字符串/数字/布尔；复杂类型存JSON
     */
    private String config_value;

    /**
     * 值类型: 'string','number','boolean','json'
     */
    private String valueType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 配置说明
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
