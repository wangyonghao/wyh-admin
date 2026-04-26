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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 站点配置
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Data
@Schema(description = "站点配置")
public class SiteConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 站点名称
     */
    @Schema(description = "站点名称", example = "WYH Admin")
    private String siteName;

    /**
     * 站点Logo
     */
    @Schema(description = "站点Logo URL", example = "")
    private String siteLogo;

    /**
     * 版权信息
     */
    @Schema(description = "版权信息", example = "Copyright © 2024 WYH Admin")
    private String siteCopyright;

    /**
     * ICP备案号
     */
    @Schema(description = "ICP备案号", example = "")
    private String siteIcp;
}
