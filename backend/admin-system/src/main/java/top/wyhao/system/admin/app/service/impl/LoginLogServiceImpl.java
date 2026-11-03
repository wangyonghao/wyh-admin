package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wyhao.audit.client.LoginEvent;
import top.wyhao.cmn.core.util.IpUtils;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.system.admin.app.assembler.LoginLogAssembler;
import top.wyhao.system.admin.app.dto.LoginLogExcelResult;
import top.wyhao.system.admin.app.dto.LoginLogQuery;
import top.wyhao.system.admin.app.dto.LoginLogResult;
import top.wyhao.system.admin.app.service.LoginLogService;
import top.wyhao.system.admin.domain.gateway.LoginLogRepository;
import top.wyhao.system.admin.domain.model.LoginLog;
import top.wyhao.system.admin.domain.model.LoginLogCriteria;
import top.wyhao.system.admin.domain.model.LoginLogStatus;
import top.wyhao.web.core.model.PageQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志 Service：监听事件落库，并提供查询 / 导出 / 清理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final LoginLogAssembler loginLogAssembler;

    @Async
    @EventListener
    @Override
    public void onLogin(LoginEvent event) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(event.getUsername());
            loginLog.setIpAddress(event.getIpAddress());
            loginLog.setUserAgent(event.getUserAgent());
            loginLog.setLoginStatus(event.isSuccess()
                    ? LoginLogStatus.SUCCESS.getValue()
                    : LoginLogStatus.FAILURE.getValue());
            loginLog.setFailureReason(event.getFailureReason());
            loginLog.setLoginTime(event.getLoginTime() != null ? event.getLoginTime() : LocalDateTime.now());
            loginLog.setDeviceType("WEB");
            loginLog.setTenantId(event.getTenantId());
            loginLog.setLocation(parseLocation(event.getIpAddress()));
            fillDeviceInfo(loginLog, event.getUserAgent());

            loginLogRepository.insert(loginLog);
            log.info("登录日志记录成功: username={}, ip={}, status={}",
                    event.getUsername(), event.getIpAddress(), loginLog.getLoginStatus());
        } catch (Exception e) {
            log.error("登录日志记录失败: username={}, ip={}, error={}",
                    event.getUsername(), event.getIpAddress(), e.getMessage(), e);
        }
    }

    @Override
    public PageResult<LoginLogResult> page(LoginLogQuery query, PageQuery pageQuery) {
        return loginLogRepository.page(toCriteria(query), pageQuery.getPage(), pageQuery.getPageSize())
                .map(loginLogAssembler::toResult);
    }

    @Override
    public void export(LoginLogQuery query, HttpServletResponse response) {
        List<LoginLog> list = loginLogRepository.list(toCriteria(query));
        List<LoginLogExcelResult> exportList = loginLogAssembler.toExcelList(list);
        ExcelUtils.export(exportList, "登录日志数据", LoginLogExcelResult.class, response);
    }

    @Override
    public int cleanExpiredLogs(int retentionDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
        int count = loginLogRepository.deleteBefore(expireTime);
        log.info("清理过期登录日志完成，清理数量: {}, 留存天数: {}", count, retentionDays);
        return count;
    }

    private LoginLogCriteria toCriteria(LoginLogQuery query) {
        LoginLogCriteria criteria = new LoginLogCriteria();
        criteria.setUsername(query.getUsername());
        criteria.setIpAddress(query.getIpAddress());
        if (query.getLoginStatus() != null) {
            criteria.setLoginStatus(query.getLoginStatus().getValue());
        }
        criteria.setLoginTimeStart(query.getLoginTimeStart());
        criteria.setLoginTimeEnd(query.getLoginTimeEnd());
        if (!LoginUtil.isSuperadmin()) {
            criteria.setTenantRestricted(true);
            criteria.setTenantId(LoginUtil.getTenantId());
        } else if (query.getTenantId() != null) {
            criteria.setTenantRestricted(true);
            criteria.setTenantId(query.getTenantId());
        }
        return criteria;
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

    private void fillDeviceInfo(LoginLog loginLog, String userAgent) {
        if (CharSequenceUtil.isBlank(userAgent)) {
            setDefaultDeviceInfo(loginLog);
            return;
        }
        try {
            UserAgent ua = UserAgentUtil.parse(userAgent);
            if (ua != null) {
                loginLog.setBrowser(parseBrowser(ua));
                loginLog.setOs(parseOs(ua));
            } else {
                setDefaultDeviceInfo(loginLog);
            }
        } catch (Exception e) {
            log.warn("解析 User-Agent 失败: error={}", e.getMessage());
            setDefaultDeviceInfo(loginLog);
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

    private void setDefaultDeviceInfo(LoginLog loginLog) {
        loginLog.setBrowser("其他");
        loginLog.setOs("其他");
    }
}
