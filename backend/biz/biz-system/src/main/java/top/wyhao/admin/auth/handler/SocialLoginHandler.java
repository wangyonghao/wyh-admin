
package top.wyhao.admin.auth.handler;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;
import top.wyhao.admin.auth.LoginHelper;
import top.wyhao.admin.auth.model.SocialLoginRequest;
import top.wyhao.admin.auth.model.LoginResult;
import top.wyhao.admin.system.model.SystemConstants;
import top.wyhao.admin.system.model.bo.MessageReq;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.admin.system.entity.user.SysUser;
import top.wyhao.admin.system.entity.user.SysUserSocial;
import top.wyhao.admin.system.model.enums.MessageTemplates;
import top.wyhao.admin.system.model.enums.MessageType;
import top.wyhao.admin.system.service.*;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.autoconfigure.application.ApplicationProperties;
import top.wyhao.starter.core.constant.RegexConstants;
import top.wyhao.starter.core.enums.GenderEnum;
import top.wyhao.starter.core.enums.RoleCodeEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.core.util.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 第三方账号登录处理器
 */
@Component
@RequiredArgsConstructor
public class SocialLoginHandler implements LoginHandler<SocialLoginRequest> {

    private final JustAuthProperties authProperties;
    private final ApplicationProperties applicationProperties;

    private final UserService userService;
    private final UserSocialService userSocialService;
    private final RoleService roleService;
    private final DeptService deptService;
    private final MessageService messageService;
    private final OperationLogService operationLogService;
    private final LoginLogService loginLogService;

    public LoginResult login(SocialLoginRequest req) {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        // 获取第三方登录信息
        AuthRequest authRequest = this.getAuthRequest(req.getSource());
        AuthCallback callback = new AuthCallback();
        callback.setCode(req.getCode());
        callback.setState(req.getState());
        AuthResponse<AuthUser> response = authRequest.login(callback);
        ValidationUtils.throwIf(!response.ok(), response.getMsg());
        AuthUser authUser = response.getData();
        // 如未绑定则自动注册新用户，保存或更新关联信息
        String source = authUser.getSource();
        String openId = authUser.getUuid();
        SysUserSocial userSocial = userSocialService.getBySourceAndOpenId(source, openId);
        SysUser user;
        if (userSocial == null) {
            String username = authUser.getUsername();
            String nickname = authUser.getNickname();
            SysUser existsUser = userService.getByUsername(username);
            String randomStr = RandomUtil.randomString(RandomUtil.BASE_CHAR, 5);
            if (existsUser != null || !ReUtil.isMatch(RegexConstants.USERNAME, username)) {
                username = randomStr + IdUtil.fastSimpleUUID();
            }
            if (!ReUtil.isMatch(RegexConstants.GENERAL_NAME, nickname)) {
                nickname = source.toLowerCase() + randomStr;
            }
            user = new SysUser();
            user.setUsername(username);
            user.setNickname(nickname);
            if (authUser.getGender() != null) {
                user.setGender(GenderEnum.getByValue(Integer.parseInt(authUser.getGender().getCode())).getValue());
            }
            user.setAvatar(authUser.getAvatar());
            user.setDeptId(SystemConstants.SUPER_DEPT_ID);
            user.setStatus(StatusEnum.ENABLE.getValue());
            userService.save(user);
            Long userId = user.getId();
            roleService.assignRolesToUser(Collections.singletonList(roleService
                .getIdByCode(RoleCodeEnum.GENERAL_USER.getCode())), userId);
            userSocial = new SysUserSocial();
            userSocial.setUserId(userId);
            userSocial.setSource(source);
            userSocial.setOpenId(openId);
            this.sendSecurityMsg(user);
        } else {
            user = BeanUtil.copyProperties(userService.detail(userSocial.getUserId()), SysUser.class);
        }
        // 检查用户状态
        checkUserStatus(user);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now());
        userSocialService.saveOrUpdate(userSocial);
        // 执行认证
        // 获取权限、角色、密码过期天数
        LoginUser loginUser = new LoginUser();
        BeanUtil.copyProperties(user, loginUser);
        loginUser.setUserId(user.getId());
        loginUser.setDeviceType("PC");

        // 登录并缓存用户信息
        LoginHelper.doLogin(loginUser);

        return LoginResult.builder()
                .code("200")
                .token(LoginUtil.getTokenValue())
                .build();
    }

    /**
     * 获取 AuthRequest
     *
     * @param source 平台名称
     * @return AuthRequest
     */
    private AuthRequest getAuthRequest(String source) {
        try {
            AuthConfig authConfig = authProperties.getType().get(source.toUpperCase());
            return AuthRequestBuilder.builder().source(source).authConfig(authConfig).build();
        } catch (Exception e) {
            throw new BusinessException("platform_not_support","暂不支持 [%s] 平台账号登录".formatted(source));
        }
    }

    /**
     * 发送安全消息
     *
     * @param user 用户信息
     */
    private void sendSecurityMsg(SysUser user) {
        MessageTemplates template = MessageTemplates.SOCIAL_REGISTER;
        MessageReq req = new MessageReq(MessageType.SECURITY);
        req.setTitle(template.getTitle().formatted(applicationProperties.getName()));
        req.setContent(template.getContent().formatted(user.getNickname()));
        messageService.add(req, CollUtil.toList(user.getId().toString()));
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
