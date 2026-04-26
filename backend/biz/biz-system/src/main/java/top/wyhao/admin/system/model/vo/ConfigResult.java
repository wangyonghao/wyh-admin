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

package top.wyhao.admin.system.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置响应信息
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "系统配置响应信息")
public class ConfigResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 配置键
     */
    @Schema(description = "配置键", example = "site")
    private String configKey;

    /**
     * 配置值（JSON格式）
     */
    @Schema(description = "配置值（JSON格式）", example = "{\"siteName\":\"WYH Admin\"}")
    private String configValue;

    /**
     * 配置说明
     */
    @Schema(description = "配置说明", example = "站点配置")
    private String description;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "版本号", example = "1")
    @JsonIgnore
    private Integer version;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-04-26 10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-04-26 10:00:00")
    private LocalDateTime updatedAt;
}
