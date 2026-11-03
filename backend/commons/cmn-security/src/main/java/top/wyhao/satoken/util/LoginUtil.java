package top.wyhao.satoken.util;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.cmn.identity.LoginUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 封装 Sa-Token：当前登录用户与会话操作。
 *
 * <p>在非 Web 请求线程（定时任务、启动初始化、MyBatis 元数据填充等）调用时，
 * 查询类方法返回 null / false，避免 SaTokenContext 未初始化导致整条 SQL 失败。
 */
public final class LoginUtil {

    public static final String LOGIN_USER_KEY = "loginUser";

    private LoginUtil() {
    }

    /**
     * 当前请求是否具备可用的 Sa-Token 上下文
     */
    private static boolean isContextReady() {
        SaTokenContext context = SaManager.getSaTokenContext();
        return context == null || !context.isValid();
    }

    /**
     * 当前登录用户 ID（需已登录）
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            StpUtil.checkLogin();
            return null;
        }
        return loginUser.getUsername();
    }

    /**
     * 当前登录用户；未登录或无请求上下文时返回 null
     */
    public static LoginUser getLoginUser() {
        if (!isLogin()) {
            return null;
        }
        SaSession session = StpUtil.getTokenSession();
        if (session == null) {
            return null;
        }
        return (LoginUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 是否超级管理员；无登录上下文时返回 false
     */
    public static boolean isSuperadmin() {
        if (!isLogin()) {
            return false;
        }
        return StpUtil.hasRole(RoleCodeEnum.SUPER_ADMIN.getCode());
    }

    public static LoginUser getLoginUser(Object loginId) {
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (LoginUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 判断当前会话是否已经登录
     */
    public static boolean isLogin() {
        if (isContextReady()) {
            return false;
        }
        return StpUtil.isLogin();
    }

    /**
     * 检验当前会话是否已经登录，如未登录，则抛出异常
     */
    public static void checkLogin() {
        StpUtil.checkLogin();
    }

    /**
     * 根据用户id 踢人下线
     *
     * @param userId 用户ID
     */
    public static void kickout(Long userId) {
        StpUtil.kickout(userId);
    }

    /**
     * 根据Token 踢人下线
     *
     * @param token token
     */
    public static void kickout(String token) {
        StpUtil.kickoutByTokenValue(token);
    }

    public static void logout() {
        StpUtil.logout();
    }

    public static List<String> getPermissions() {
        if (!isLogin()) {
            return Collections.emptyList();
        }
        return StpUtil.getPermissionList();
    }

    public static List<String> getPermissions(String userId) {
        return StpUtil.getPermissionList(userId);
    }

    public static boolean hasPermission(String permission) {
        if (!isLogin()) {
            return false;
        }
        return StpUtil.hasPermission(permission);
    }

    public static boolean hasRole(String role) {
        if (!isLogin()) {
            return false;
        }
        return StpUtil.hasRole(role);
    }

    /**
     * 查询已登录用户信息
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param ascend   升序
     * @return 登录用户信息
     */
    public static List<LoginUser> pageUser(String keyword, int page, int pageSize, boolean ascend) {
        int start = (page - 1) * pageSize;
        List<String> sessionIdList = StpUtil.searchTokenSessionId(keyword, start, pageSize, ascend);
        List<LoginUser> users = new ArrayList<>();
        for (String sessionId : sessionIdList) {
            SaSession session = StpUtil.getSessionBySessionId(sessionId);
            if (session != null) {
                users.add((LoginUser) session.get(LOGIN_USER_KEY));
            }
        }
        return users;
    }

    public static String getTokenValue() {
        if (isContextReady()) {
            return null;
        }
        return StpUtil.getTokenValue();
    }

    public static Long getTenantId() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return null;
        }
        if (loginUser.getTenantId() != null) {
            return loginUser.getTenantId();
        }
        SaSession session = StpUtil.getTokenSession();
        if (session == null) {
            return null;
        }
        Object raw = session.get("tenantId");
        if (raw == null) {
            return null;
        }
        try {
            return Long.valueOf(raw.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 更新当前会话中的租户 / 组织 ID。
     */
    public static void setTenantId(Long tenantId) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            StpUtil.checkLogin();
            return;
        }
        loginUser.setTenantId(tenantId);
        SaSession session = StpUtil.getTokenSession();
        if (session != null) {
            session.set("tenantId", tenantId);
            session.set(LOGIN_USER_KEY, loginUser);
        }
    }
}
