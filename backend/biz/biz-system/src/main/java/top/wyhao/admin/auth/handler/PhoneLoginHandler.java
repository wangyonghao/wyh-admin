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

package top.wyhao.admin.auth.handler;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.admin.auth.model.bo.PhoneLoginRequest;
import top.wyhao.admin.auth.model.vo.LoginResult;
import top.wyhao.admin.system.model.entity.DeptDO;
import top.wyhao.admin.system.model.entity.user.UserDO;
import top.wyhao.admin.system.service.DeptService;
import top.wyhao.admin.system.service.OperationLogService;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.core.util.validation.ValidationUtils;

/**
 * 手机号登录处理器
 */
@RequiredArgsConstructor
@Component
public class PhoneLoginHandler implements LoginHandler<PhoneLoginRequest> {
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final DeptService deptService;

    public LoginResult login(PhoneLoginRequest req) {
        this.preLogin(req);
        // 验证手机号
        UserDO user = userService.getByPhone(req.getPhone());
        ValidationUtils.throwIfNull(user, "此手机号未绑定本系统账号");
        // 检查用户状态
        checkUserStatus(user);
        // 执行认证
        // 获取权限、角色、密码过期天数
        LoginUser loginUser = new LoginUser();
        BeanUtil.copyProperties(user, loginUser);
        loginUser.setUserId(user.getId());
        loginUser.setDeviceType("PC");

        // 登录并缓存用户信息
        LoginUtil.doLogin(loginUser);

        // 记录登录日志
        operationLogService.recordLoginLog(loginUser);

        return LoginResult.builder()
                .code("200")
                .token(LoginUtil.getTokenValue())
                .build();
    }

    public void preLogin(PhoneLoginRequest req) {
        String phone = req.getPhone();
        String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + phone;
        String captcha = RedisUtils.get(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        ValidationUtils.throwIfNotEqualIgnoreCase(req.getCaptcha(), captcha, "验证码已失效");
        RedisUtils.delete(captchaKey);
    }

    /**
     * 检查用户状态
     *
     * @param user 用户信息
     */
    private void checkUserStatus(UserDO user) {
        BizAssert.throwIfEqual(StatusEnum.DISABLE, user.getStatus(), "此账号已被禁用，如有疑问，请联系管理员");
        DeptDO dept = deptService.getById(user.getDeptId());
        BizAssert.throwIfEqual(StatusEnum.DISABLE, dept.getStatus(), "此账号所属部门已被禁用，如有疑问，请联系管理员");
    }
}