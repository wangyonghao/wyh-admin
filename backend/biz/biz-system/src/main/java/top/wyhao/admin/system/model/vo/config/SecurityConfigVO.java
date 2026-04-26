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
 * 安全配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "安全配置")
public class SecurityConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 密码最小长度
     */
    @Schema(description = "密码最小长度", example = "8")
    private Integer passwordMinLength;

    /**
     * 密码是否需要大写字母
     */
    @Schema(description = "密码是否需要大写字母", example = "true")
    private Boolean passwordRequireUppercase;

    /**
     * 密码是否需要小写字母
     */
    @Schema(description = "密码是否需要小写字母", example = "true")
    private Boolean passwordRequireLowercase;

    /**
     * 密码是否需要数字
     */
    @Schema(description = "密码是否需要数字", example = "true")
    private Boolean passwordRequireNumber;

    /**
     * 密码是否需要特殊字符
     */
    @Schema(description = "密码是否需要特殊字符", example = "false")
    private Boolean passwordRequireSpecial;

    /**
     * 会话超时时间（分钟）
     */
    @Schema(description = "会话超时时间（分钟）", example = "30")
    private Integer sessionTimeout;
}
