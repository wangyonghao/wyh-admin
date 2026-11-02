package top.wyhao.system.auth.domain.user;

import cn.hutool.core.util.ReUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.domain.gateway.CaptchaGateway;
import top.wyhao.system.auth.domain.gateway.PasswordEncodeGateway;
import top.wyhao.system.auth.domain.gateway.PasswordFailGateway;
import top.wyhao.system.auth.domain.model.ClientContext;
import top.wyhao.system.auth.domain.model.ImageCaptcha;

/**
 * 密码登录安全
 * 1. 按账号和 IP 双维度防爆破
 * 2. 渐进式延迟、风险触发验证码
 * 3. 校验慢哈希密码
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PasswordSecurityService {
    /** 密码失败计数 */
    private final PasswordFailGateway failCountGateway;
    /** 密码慢哈希算法 */
    private final PasswordEncodeGateway passwordEncodeGateway;
    /** 图形验证码生成 / 校验 */
    private final CaptchaGateway captchaGateway;

    /**
     * 按账号和 IP 双维度防爆破
     */
    public void assertNotBanned(String account, String ip) {
        if (failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, ip)) {
            throw AuthenticationException.passwordErrorExceeded("用户 ["+account+"] 在 IP ["+ip+"] 上由于尝试次数过多已被禁止登录");
        }
        if (failCountGateway.isBanned(PasswordFailGateway.LimitDimension.ACCOUNT, account)) {
            throw AuthenticationException.accountLocked(
                    failCountGateway.getRemainMinutes(PasswordFailGateway.LimitDimension.ACCOUNT, account));
        }
    }

    /**
     * 渐进式延迟、风险触发验证码
     */
    public void verifyCaptchaIfNeeded(String account, String uuid, String captcha) {
        if (needCaptcha(account)) {
            captchaGateway.verify(uuid, captcha);
        }
    }

    /**
     * 生成登录图形验证码；功能关闭时返回空图。
     */
    public ImageCaptcha createCaptcha() {
        PasswordPolicy policy = failCountGateway.currentPolicy();
        if (!policy.captchaFeatureEnabled()) {
            return ImageCaptcha.disabled();
        }
        return captchaGateway.generate().withAlwaysRequired(policy.captchaAlwaysRequired());
    }

    /**
     * 密码次数超过设置值时，才需要验证码
     */
    public boolean needCaptcha(String account) {
        int failCount = failCountGateway.getFailCount(account);
        PasswordPolicy policy = failCountGateway.currentPolicy();

        int threshold = policy.captchaErrorThreshold();
        if (threshold < 0) {
            return false;
        }
        return failCount >= threshold;
    }

    /**
     * 校验慢哈希密码
     */
    public void verifyPassword(CredentialUser user, String password, ClientContext client) {
        if (!passwordEncodeGateway.matches(password, user.password())) {
            rejectCredential(user.username(), client, "密码不匹配");
        }
        failCountGateway.clear(user.username());
    }

    public void fastVerifyPasswordFormat(String account, String password, ClientContext client) {
        if (!ReUtil.isMatch(RegexConstants.PASSWORD, password)) {
            rejectCredential(account, client, "密码格式错误");
        }
    }

    /**
     * 用户不存在、密码错、格式错等凭证失败：计数、可能锁定/要求验证码，前端统一模糊文案。
     */
    public void rejectCredential(String account, ClientContext client, String realReason) {
        PasswordPolicy policy = failCountGateway.currentPolicy();
        String ip = client != null ? client.ip() : null;
        failCountGateway.increment(account, ip, policy.lockTimeMinutes());

        if (failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, ip)) {
            throw AuthenticationException.passwordErrorExceeded(realReason);
        }
        if (failCountGateway.isBanned(PasswordFailGateway.LimitDimension.ACCOUNT, account)) {
            throw AuthenticationException.accountLocked(
                    failCountGateway.getRemainMinutes(PasswordFailGateway.LimitDimension.ACCOUNT, account),
                    realReason);
        }
        if (needCaptcha(account)) {
            throw AuthenticationException.needCaptcha(realReason);
        }
        throw AuthenticationException.passwordError(realReason);
    }
}
