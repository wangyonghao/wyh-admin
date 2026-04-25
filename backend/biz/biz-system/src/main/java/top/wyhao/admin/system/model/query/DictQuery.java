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
import top.wyhao.starter.data.annotation.Query;
import top.wyhao.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典查询条件
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Data
@Schema(description = "字典查询条件")
public class DictQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词")
    @Query(columns = {"dict_type", "label", "value", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型", example = "notice_type")
    @Query(type = QueryType.EQ)
    private String dictType;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    @Query(type = QueryType.EQ)
    private Boolean enabled;
}