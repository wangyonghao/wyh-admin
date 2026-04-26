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

package top.wyhao.admin.system.model.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "注册配置")
public class RegisterConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否开启注册
     */
    @Schema(description = "是否开启注册", example = "true")
    private Boolean enabled;

    /**
     * 注册是否需要邮箱验证
     */
    @Schema(description = "注册是否需要邮箱验证", example = "false")
    private Boolean verifyEmail;

    /**
     * 注册是否需要手机验证
     */
    @Schema(description = "注册是否需要手机验证", example = "false")
    private Boolean verifyPhone;

    /**
     * 注册默认角色ID
     */
    @Schema(description = "注册默认角色ID", example = "")
    private String defaultRoleId;
}
