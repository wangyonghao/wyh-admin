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

import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import top.wyhao.admin.auth.model.bo.AccountLoginRequest;
import top.wyhao.admin.auth.model.vo.LoginResult;
import top.wyhao.admin.modules.common.util.RsaUtils;
import top.wyhao.admin.system.model.entity.DeptDO;
import top.wyhao.admin.system.model.entity.user.UserDO;
import top.wyhao.admin.system.service.DeptService;
import top.wyhao.admin.system.service.OperationLogService;
import top.wyhao.admin.system.service.SettingsService;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.starter.web.ServletUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 账号登录处理器
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/22 14:58
 */
@Component
@RequiredArgsConstructor
public class AccountLoginHandler implements LoginHandler<AccountLoginRequest>{

    private static final String LOGIN_RETRY_KEY="login:retry:";
    private static final String CAPTCHA_KEY = "login:captcha:";

    private final PasswordEncoder passwordEncoder;
    private final SettingsService settingsService;
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final DeptService deptService;

    public LoginResult login(AccountLoginRequest req) {
        // 解密密码
        String password = RsaUtils.decryptPasswordByRsaPrivateKey(req.getPassword(), "密码解密失败");
        String ip = ServletUtils.getRequestIp();

        // 1. 校验图片验证码
        validateCaptcha(req);

        // 2. 重试次数检查
        String retryKey = LOGIN_RETRY_KEY + req.getUsername() +":"+ ip;
        checkRetryLimit(retryKey);

        // 3. 用户验证
        UserDO user = userService.getByUsername(req.getUsername());

        if(Objects.isNull(user)) {
            incrementRetry(retryKey);
            // 防止用户名探测，统一提示：用户名或密码错误
            throw new BusinessException("USERNAME_PASSWORD_ERROR", "用户名或密码错误");
        }

        // 4. 校验用户密码
        if(!passwordEncoder.matches(password, user.getPassword())){
            incrementRetry(retryKey);
            int remaining = settingsService.getMaxRetryCount() - getRetryCount(retryKey);
            String msg = remaining > 0 ? "用户名或密码错误，还剩" + remaining + "次机会" : "用户名或密码错误，账号已锁定";
            throw new BusinessException("USERNAME_PASSWORD_ERROR",msg);
        }else{
            // 如账号密码匹配，清除错误次数
            clearRetryCount(retryKey);
        }

        // 5. 检查用户状态
        checkUserStatus(user);

        // 6. 密码过期时，强制用户修改密码
        if(user.getPwdExpireDate() != null && LocalDate.now().isAfter(user.getPwdExpireDate())){
            String tempToken = SaTempUtil.createToken(user.getId(), 600); // 10分钟
            return LoginResult.builder().code("PASSWORD_EXPIRED").token(tempToken).build();
        }

        // 8. 生成Token，缓存登录用户信息
        LoginUser loginUser = BeanUtil.copyProperties(user, LoginUser.class);
        loginUser.setUserId(user.getId());
        loginUser.setDeviceType("PC");
        LoginUtil.doLogin(loginUser);

        // 9. 记录登录日志
        operationLogService.recordLoginLog(loginUser);

        return LoginResult.builder()
                .code("200")
                .token(LoginUtil.getTokenValue())
                .build();
    }

    private void validateCaptcha(AccountLoginRequest req) {
        // 校验验证码
        boolean loginCaptchaEnabled = settingsService.isLoginCaptchaEnabled();
        if (!loginCaptchaEnabled) {
            return;
        }
        ValidationUtils.throwIfBlank(req.getCaptcha(), "验证码不能为空");
        ValidationUtils.throwIfBlank(req.getUuid(), "验证码标识不能为空");
        String captcha = RedisUtils.get(CAPTCHA_KEY + req.getUuid());
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        RedisUtils.delete(CAPTCHA_KEY + req.getUuid());
        ValidationUtils.throwIfNotEqualIgnoreCase(req.getCaptcha(), captcha, "验证码不正确");
    }

    private void incrementRetry(String retryKey) {
        RedisUtils.incr(retryKey);
        RedisUtils.expire(retryKey, Duration.ofMinutes(settingsService.getLockMinutes()));
    }

    private int getRetryCount(String retryKey) {
        Integer count = RedisUtils.get(retryKey);
        return count != null ? count : 0;
    }

    /**
     * 清除密码错误次数
     * @param retryKey 密码错误次数缓存的Key
     */
    private void clearRetryCount(String retryKey){
        RedisUtils.delete(retryKey);
    }

    /**
     * 密码错误次数超过限制则锁定账号
     * @param retryKey 密码错误次数缓存的Key
     */
    private void checkRetryLimit(String retryKey){
        int retryCount = getRetryCount(retryKey);
        int maxRetry = settingsService.getMaxRetryCount();
        if(retryCount >= maxRetry){
            long ttl = RedisUtils.getTimeToLive(retryKey);
            throw new BusinessException("ACCOUNT_LOCKED","账号已锁定, %s 分钟后可重试".formatted(ttl / 60));
        }
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