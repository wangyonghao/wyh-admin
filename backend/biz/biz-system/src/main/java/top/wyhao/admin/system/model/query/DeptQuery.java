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
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.data.annotation.Query;
import top.wyhao.starter.data.enums.QueryType;
import top.wyhao.starter.web.core.model.SortQuery;

/**
 * 部门查询条件
 *
 * @author Charles7c
 * @since 2023/1/22 17:52
 */
@Data
@Schema(description = "部门查询条件")
public class DeptQuery extends SortQuery {
    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "测试部")
    @Query(columns = {"name", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = QueryType.EQ)
    private StatusEnum status;
}
