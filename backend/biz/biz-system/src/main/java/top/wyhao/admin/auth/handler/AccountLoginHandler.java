
package top.wyhao.admin.auth.handler;

import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import top.wyhao.admin.auth.LoginHelper;
import top.wyhao.admin.auth.model.AccountLoginRequest;
import top.wyhao.admin.auth.model.LoginResult;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.admin.system.entity.user.SysUser;
import top.wyhao.admin.system.model.vo.config.LoginConfigVO;
import top.wyhao.admin.system.service.*;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.UserContextHolder;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.util.RsaUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.http.ServletUtils;

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
public class AccountLoginHandler implements LoginHandler<AccountLoginRequest> {

    private static final String LOGIN_RETRY_KEY = "login:retry:";
    private static final String CAPTCHA_KEY = "login:captcha:";

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final LoginLogService loginLogService;
    private final DeptService deptService;
    private final ConfigService configService;

    public LoginResult login(AccountLoginRequest req) {
        // 解密密码
        String password = RsaUtils.decryptPasswordByRsaPrivateKey(req.getPassword(), "密码解密失败");
        String ip = ServletUtils.getRequestIp();
        HttpServletRequest request = ServletUtils.getRequest();
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        try {
            // 1. 校验图片验证码
            validateCaptcha(req);

            // 2. 重试次数检查
            String retryKey = LOGIN_RETRY_KEY + req.getUsername() + ":" + ip;
            checkRetryLimit(retryKey);

            // 3. 用户验证
            SysUser user = userService.getByUsername(req.getUsername());

            if (Objects.isNull(user)) {
                incrementRetry(retryKey);
                // 记录登录失败日志
                loginLogService.create(req.getUsername(), ip, userAgent, "FAILURE", "用户不存在");
                // 防止用户名探测，统一提示：用户名或密码错误
                throw new BusinessException("USERNAME_PASSWORD_ERROR", "用户名或密码错误");
            }

            // 4. 校验用户密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                incrementRetry(retryKey);
                LoginConfigVO loginConfig = configService.getLoginConfig();
                int remaining = loginConfig.getMaxRetry() - getRetryCount(retryKey);
                String msg = remaining > 0 ? "用户名或密码错误，还剩" + remaining + "次机会" : "用户名或密码错误，账号已锁定";
                // 记录登录失败日志
                loginLogService.create(req.getUsername(), ip, userAgent, "FAILURE", "密码错误");
                throw new BusinessException("USERNAME_PASSWORD_ERROR", msg);
            } else {
                // 如账号密码匹配，清除错误次数
                clearRetryCount(retryKey);
            }

            // 5. 检查用户状态
            checkUserStatus(user);

            // 6. 密码过期时，强制用户修改密码
            if (user.getPwdExpireDate() != null && LocalDate.now().isAfter(user.getPwdExpireDate())) {
                String tempToken = SaTempUtil.createToken(user.getId(), 600); // 10分钟
                // 记录登录失败日志（密码过期）
                loginLogService.create(req.getUsername(), ip, userAgent, "FAILURE", "密码已过期");
                return LoginResult.builder().code("PASSWORD_EXPIRED").token(tempToken).build();
            }

            // 8. 生成Token，缓存登录用户信息
            LoginUser loginUser = BeanUtil.copyProperties(user, LoginUser.class);
            loginUser.setUserId(user.getId());
            loginUser.setDeviceType("PC");
            LoginHelper.doLogin(loginUser);

            return LoginResult.builder()
                    .code("200")
                    .token(UserContextHolder.getToken())
                    .build();
        } catch (BusinessException e) {
            // 如果是业务异常且还没记录日志，记录失败日志
            if (!"USERNAME_PASSWORD_ERROR".equals(e.getCode())) {
                loginLogService.create(req.getUsername(), ip, userAgent, "FAILURE", e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            // 其他异常也记录失败日志
            loginLogService.create(req.getUsername(), ip, userAgent, "FAILURE", "系统异常: " + e.getMessage());
            throw e;
        }
    }

    private void validateCaptcha(AccountLoginRequest req) {
        // 校验验证码
        LoginConfigVO configVO = configService.getLoginConfig();
        boolean loginCaptchaEnabled = configVO.getCaptchaEnabled();
        if (!loginCaptchaEnabled) {
            return;
        }
        if (StrUtil.isBlank(req.getCaptcha())) {
            throw new BusinessException("CAPTCHA_IS_REQUIRED", "验证码不能为空");
        }
        if (StrUtil.isBlank(req.getUuid())) {
            throw new BusinessException("CAPTCHA_UUID_IS_REQUIRED", "验证码标识不能为空");
        }
        String captcha = RedisUtils.getAndDelete(CAPTCHA_KEY + req.getUuid());
        if (StrUtil.isBlank(req.getUuid())) {
            throw new BusinessException("CAPTCHA_EXPIRED", "验证码已失效");
        }
        if (!StrUtil.equalsIgnoreCase(req.getCaptcha(), captcha)) {
            throw new BusinessException("CAPTCHA_ERROR", "验证码不正确");
        }

    }

    private void incrementRetry(String retryKey) {
        RedisUtils.incr(retryKey);
        LoginConfigVO configVO = configService.getLoginConfig();
        RedisUtils.expire(retryKey, Duration.ofMinutes(configVO.getLockTime()));
    }

    private int getRetryCount(String retryKey) {
        Integer count = RedisUtils.get(retryKey);
        return count != null ? count : 0;
    }

    /**
     * 清除密码错误次数
     *
     * @param retryKey 密码错误次数缓存的Key
     */
    private void clearRetryCount(String retryKey) {
        RedisUtils.delete(retryKey);
    }

    /**
     * 密码错误次数超过限制则锁定账号
     *
     * @param retryKey 密码错误次数缓存的Key
     */
    private void checkRetryLimit(String retryKey) {
        int retryCount = getRetryCount(retryKey);
        LoginConfigVO loginConfig = configService.getLoginConfig();
        int maxRetry = loginConfig.getMaxRetry();
        if (retryCount >= maxRetry) {
            long ttl = RedisUtils.getTimeToLive(retryKey);
            throw new BusinessException("ACCOUNT_LOCKED", "账号已锁定, %s 分钟后可重试".formatted(ttl / 60));
        }
    }


    /**
     * 检查用户状态
     *
     * @param user 用户信息
     */
    private void checkUserStatus(SysUser user) {
        BizAssert.throwIfEqual(StatusEnum.DISABLE, user.getStatus(), "此账号已被禁用，如有疑问，请联系管理员");
        SysDept dept = deptService.getById(user.getDeptId());
        BizAssert.throwIfEqual(StatusEnum.DISABLE, dept.getStatus(), "此账号所属部门已被禁用，如有疑问，请联系管理员");
    }

}