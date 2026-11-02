package top.wyhao.system.auth.infrastructure.gateway;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.util.IpUtils;
import top.wyhao.system.auth.domain.authlog.AuthLog;
import top.wyhao.system.auth.domain.authlog.AuthLogDomainService;
import top.wyhao.system.auth.domain.authlog.AuthLogStatus;
import top.wyhao.system.auth.domain.gateway.AuthLogGateway;

import java.time.LocalDateTime;

/**
 * 登录审计写路径：组装、解析 IP/UA、异步落库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthLogGatewayImpl implements AuthLogGateway {

    private final AuthLogDomainService authLogDomainService;

    @Async
    @Override
    public void recordSuccess(String username, String ip, String userAgent) {
        asyncWrite(username, ip, userAgent, AuthLogStatus.SUCCESS.getValue(), null);
    }

    @Async
    @Override
    public void recordFailure(String username, String ip, String userAgent, String reason) {
        asyncWrite(username, ip, userAgent, AuthLogStatus.FAILURE.getValue(), reason);
    }

    private void asyncWrite(String username, String ipAddress, String userAgent, String loginStatus, String failureReason) {
        try {
            AuthLog loginLog = new AuthLog();
            loginLog.setUsername(username);
            loginLog.setIpAddress(ipAddress);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginStatus(loginStatus);
            loginLog.setFailureReason(failureReason);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setDeviceType("WEB");
            loginLog.setLocation(parseLocation(ipAddress));

            if (CharSequenceUtil.isNotBlank(userAgent)) {
                UserAgent ua = UserAgentUtil.parse(userAgent);
                if (ua != null) {
                    loginLog.setBrowser(parseBrowser(ua));
                    loginLog.setOs(parseOs(ua));
                } else {
                    setDefaultDeviceInfo(loginLog);
                }
            } else {
                setDefaultDeviceInfo(loginLog);
            }

            authLogDomainService.insert(loginLog);
            log.info("登录日志记录成功: username={}, ip={}, status={}", username, ipAddress, loginStatus);
        } catch (Exception e) {
            log.error("登录日志记录失败: username={}, ip={}, error={}", username, ipAddress, e.getMessage(), e);
        }
    }

    private String parseLocation(String ipAddress) {
        if (CharSequenceUtil.isBlank(ipAddress)) {
            return "未知地址";
        }
        try {
            String location = IpUtils.getRegion(ipAddress);
            return CharSequenceUtil.isNotBlank(location) ? location : "未知地址";
        } catch (Exception e) {
            log.warn("解析IP地址失败: ip={}, error={}", ipAddress, e.getMessage());
            return "未知地址";
        }
    }

    private String parseBrowser(UserAgent ua) {
        try {
            if (ua.getBrowser() != null) {
                String browserName = ua.getBrowser().getName();
                String version = ua.getVersion();
                if (CharSequenceUtil.isNotBlank(version)) {
                    return browserName + " " + version;
                }
                return browserName;
            }
        } catch (Exception e) {
            log.warn("解析浏览器信息失败: error={}", e.getMessage());
        }
        return "其他";
    }

    private String parseOs(UserAgent ua) {
        try {
            if (ua.getOs() != null) {
                return ua.getOs().getName();
            }
        } catch (Exception e) {
            log.warn("解析操作系统信息失败: error={}", e.getMessage());
        }
        return "其他";
    }

    private void setDefaultDeviceInfo(AuthLog loginLog) {
        loginLog.setBrowser("其他");
        loginLog.setOs("其他");
    }
}
