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
 * 存储配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "存储配置")
public class StorageConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 存储类型
     */
    @Schema(description = "存储类型：local, oss, s3", example = "local")
    private String type;

    /**
     * 存储端点
     */
    @Schema(description = "存储端点", example = "")
    private String endpoint;

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
     * 存储桶名称
     */
    @Schema(description = "存储桶名称", example = "")
    private String bucket;
}
