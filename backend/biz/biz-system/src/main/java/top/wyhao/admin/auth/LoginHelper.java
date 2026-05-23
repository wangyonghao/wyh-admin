package top.wyhao.admin.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.wyhao.admin.system.service.LoginLogService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.util.ExceptionUtils;
import top.wyhao.starter.core.util.IpUtils;

/**
 * 登录帮助类

 */
public class LoginHelper {
    public static void doLogin(LoginUser loginUser) {
        RequestMeta info = getRequestMeta();
        StpUtil.login(loginUser.getUserId());

        // 写入Session
        SaSession session = StpUtil.getTokenSession();
        session.set("loginName", loginUser.getUsername());
        session.set("ipaddr", info.ip());
        session.set("loginLocation", info.address());
        session.set("browser", info.browser());
        session.set("os", info.os());
        session.set("loginTime", System.currentTimeMillis());
        session.set(LoginUtil.LOGIN_USER_KEY, loginUser);

        LoginLogService loginLogService = SpringUtil.getBean(LoginLogService.class);
        // 记录登录日志
        loginLogService.asyncLog(loginUser.getUsername(), info.ip(), info.userAgent(), "SUCCESS", null);
    }

    /**
     * 从请求中读取 ip、地址、浏览器、操作系统信息
     */
    public static RequestMeta getRequestMeta() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        String ip = request != null ? JakartaServletUtil.getClientIP(request) : null;
        String address = ExceptionUtils.exToNull(() -> IpUtils.getRegion(ip));
        UserAgent ua = request != null ? UserAgentUtil.parse(request.getHeader("User-Agent")) : null;
        String browser = ua != null ? ua.getBrowser().getName() : "Unknown";
        String os = ua != null ? ua.getOs().getName() : "Unknown";
        String uaStr = ua != null ? ua.toString() : "Unknown";
        return new RequestMeta(ip, address, uaStr, browser, os);
    }


    /**
     * 请求信息
     */
    public record RequestMeta(String ip, String address, String userAgent, String browser, String os) {
    }
}
