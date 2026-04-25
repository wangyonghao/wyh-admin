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

package top.wyhao.admin.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志实体
 *
 * @author Charles7c
 * @since 2022/12/25 9:11
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId
    private Long id;
    /**
     * 业务对象类型 如：user、sms、login、bug、task、project
     */
    private String objectType;

    /**
     * 业务对象ID
     */
    private Long objectId;

    /**
     * 操作类型 如：login、send、create、update、delete、logout
     */
    private String operation;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 操作者名称
     */
    private String operatorName;

    /**
     * 操作者IP
     */
    private String operatorIp;

    /**
     * 操作者位置（从IP推断的位置）
     */
    private String operatorLocation;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 状态 success / fail
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 额外信息 JSON格式
     */
    private String extra;
}
