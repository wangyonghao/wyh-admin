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

package top.wyhao.admin.system.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统配置请求信息
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "系统配置请求信息")
public class ConfigRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置键
     */
    @Schema(description = "配置键", example = "site")
    @NotBlank(message = "配置键不能为空", groups = {Create.class})
    @Size(max = 100, message = "配置键长度不能超过 100 个字符", groups = {Create.class, Update.class})
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
    @Size(max = 255, message = "配置说明长度不能超过 255 个字符", groups = {Create.class, Update.class})
    private String description;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "版本号", example = "1")
    private Integer version;

    /**
     * 创建校验组
     */
    public interface Create {
    }

    /**
     * 更新校验组
     */
    public interface Update {
    }
}
