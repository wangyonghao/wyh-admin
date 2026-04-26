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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "邮件配置")
public class EmailConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SMTP服务器地址
     */
    @Schema(description = "SMTP服务器地址", example = "smtp.example.com")
    private String host;

    /**
     * SMTP端口
     */
    @Schema(description = "SMTP端口", example = "465")
    private Integer port;

    /**
     * 发件人邮箱
     */
    @Schema(description = "发件人邮箱", example = "admin@example.com")
    private String username;

    /**
     * 邮箱密码/授权码（敏感字段）
     */
    @Schema(description = "邮箱密码/授权码", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 发件人名称
     */
    @Schema(description = "发件人名称", example = "WYH Admin")
    private String fromName;
}
