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

package top.wyhao.admin.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统配置查询条件
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "系统配置查询条件")
public class ConfigQuery {

    /**
     * 配置键（模糊查询）
     */
    @Schema(description = "配置键", example = "site")
    private String configKey;

    /**
     * 关键词（搜索配置键或描述）
     */
    @Schema(description = "关键词", example = "站点")
    private String searchWords;
}
