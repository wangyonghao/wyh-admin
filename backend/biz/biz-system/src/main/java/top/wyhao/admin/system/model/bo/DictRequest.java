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
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wyhao.starter.core.constant.RegexConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 字典创建或修改请求参数
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Data
@Schema(description = "字典创建或修改请求参数")
public class DictRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型", example = "notice_type")
    @NotBlank(message = "字典类型不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_CODE, message = "字典类型长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String dictType;

    /**
     * 字典值
     */
    @Schema(description = "字典值", example = "1")
    @NotBlank(message = "字典值不能为空")
    @Length(max = 255, message = "字典值长度不能超过 {max} 个字符")
    private String value;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签", example = "产品新闻")
    @NotBlank(message = "字典标签不能为空")
    @Length(max = 255, message = "字典标签长度不能超过 {max} 个字符")
    private String label;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息", example = "{\"color\": \"primary\"}")
    private Map<String, Object> extra;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "公告类型描述信息")
    @Length(max = 500, message = "描述长度不能超过 {max} 个字符")
    private String description;
}