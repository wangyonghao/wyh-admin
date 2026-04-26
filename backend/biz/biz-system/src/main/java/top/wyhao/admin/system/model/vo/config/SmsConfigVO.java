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
 * 短信配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "短信配置")
public class SmsConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短信服务商
     */
    @Schema(description = "短信服务商：aliyun, tencent", example = "aliyun")
    private String provider;

    /**
     * AccessKey（敏感字段）
     */
    @Schema(description = "AccessKey", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String accessKey;

    /**
     * SecretKey（敏感字段）
     */
    @Schema(description = "SecretKey", example = "******")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String secretKey;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名", example = "")
    private String signName;
}
