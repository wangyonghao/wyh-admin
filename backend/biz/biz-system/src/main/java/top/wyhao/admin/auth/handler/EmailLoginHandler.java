
package top.wyhao.admin.auth.handler;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.admin.auth.LoginHelper;
import top.wyhao.admin.auth.model.EmailLoginRequest;
import top.wyhao.admin.auth.model.LoginResult;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.admin.system.entity.SysUser;
import top.wyhao.admin.system.service.DeptService;
import top.wyhao.admin.system.service.LoginLogService;
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
 * 邮箱登录处理器
 */

@RequiredArgsConstructor
@Component
public class EmailLoginHandler implements LoginHandler<EmailLoginRequest> {
    private final UserService userService;
    private final OperationLogService operationLogService;
    private final DeptService deptService;
    private final LoginLogService loginLogService;

    public LoginResult login(EmailLoginRequest req) {
        String email = req.getEmail();
        String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + email;
        String captcha = RedisUtils.get(captchaKey);
        ValidationUtils.throwIfBlank(captcha, "验证码已失效");
        ValidationUtils.throwIfNotEqualIgnoreCase(req.getCaptcha(), captcha, "验证码不正确");
        RedisUtils.delete(captchaKey);
        // 验证邮箱
        SysUser user = userService.getByEmail(req.getEmail());
        ValidationUtils.throwIfNull(user, "此邮箱未绑定本系统账号");
        // 检查用户状态
        checkUserStatus(user);
        // 执行认证
        // 获取权限、角色、密码过期天数
        LoginUser loginUser = new LoginUser();
        BeanUtil.copyProperties(user, loginUser);
        loginUser.setUserId(user.getId());
        loginUser.setDeviceType("PC");

        // 登录并记录登录日志
        LoginHelper.doLogin(loginUser);

        return LoginResult.builder()
                .code("200")
                .token(LoginUtil.getTokenValue())
                .build();
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