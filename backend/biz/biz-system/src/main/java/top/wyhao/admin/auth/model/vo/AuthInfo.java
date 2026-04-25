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

package top.wyhao.admin.auth.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.vo.MenuTreeVO;
import top.wyhao.admin.system.model.vo.user.UserDetailResult;

import java.util.List;

/**
 * 登录用户响应参数
 *
 * @author Charles7c
 * @since 2022/12/29 20:15
 */
@Data
@Schema(description = "登录用户响应参数")
public class AuthInfo {
    /**
     * 用户信息
     */
    @Schema(description = "用户信息", example = "用户信息")
    private UserDetailResult user;
    /**
     * 角色编码集合
     */
    @Schema(description = "角色编码集合", example = "[\"test\"]")
    private List<String> roles;
    /**
     * 权限码集合
     */
    @Schema(description = "权限码集合", example = "[\"system:user:list\",\"system:user:create\"]")
    private List<String> permissions;

    @Schema(description = "用户菜单", example = "")
    private List<MenuTreeVO> menus;
}
