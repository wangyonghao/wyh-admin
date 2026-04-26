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
 * 登录配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "登录配置")
public class LoginConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否开启验证码
     */
    @Schema(description = "是否开启验证码", example = "true")
    private Boolean captchaEnabled;

    /**
     * 验证码类型
     */
    @Schema(description = "验证码类型：graphic-图形验证码，behavior-行为验证码", example = "graphic")
    private String captchaType;

    /**
     * 最大重试次数
     */
    @Schema(description = "登录最大重试次数", example = "5")
    private Integer maxRetry;

    /**
     * 锁定时间（分钟）
     */
    @Schema(description = "登录锁定时间（分钟）", example = "30")
    private Integer lockTime;
}
