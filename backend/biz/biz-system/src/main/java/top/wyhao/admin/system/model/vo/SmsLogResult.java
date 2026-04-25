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

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.ResultStatusEnum;
import top.wyhao.starter.core.constant.ContainerConstants;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.time.LocalDateTime;

/**
 * 短信日志响应参数
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "短信日志响应参数")
public class SmsLogResult {

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    @Assemble(container = ContainerConstants.USER_NICKNAME, props = @Mapping(ref = "createUserString"))
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 配置 ID
     */
    @Schema(description = "配置 ID", example = "")
    @ExcelProperty(value = "配置 ID")
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "18888888888")
    @ExcelProperty(value = "手机号")
    private String phone;

    /**
     * 参数配置
     */
    @Schema(description = "参数配置")
    @ExcelProperty(value = "参数配置")
    private String params;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态", example = "1")
    @ExcelProperty(value = "发送状态", converter = ExcelBaseEnumConverter.class)
    private ResultStatusEnum status;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    @ExcelProperty(value = "返回数据")
    private String resMsg;
}