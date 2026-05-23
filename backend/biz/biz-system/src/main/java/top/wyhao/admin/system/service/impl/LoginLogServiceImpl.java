package top.wyhao.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.entity.SysLoginLog;
import top.wyhao.admin.system.mapper.SysLoginLogMapper;
import top.wyhao.admin.system.model.enums.LoginDeviceEnum;
import top.wyhao.admin.system.model.query.LoginLogQuery;
import top.wyhao.admin.system.model.vo.log.LoginLogExportResult;
import top.wyhao.admin.system.model.vo.log.LoginLogResult;
import top.wyhao.admin.system.service.LoginLogService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.UserContextHolder;
import top.wyhao.starter.core.util.IpUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志 Service 实现
 *

 * @since 2026/05/08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Async
    @Override
    public void asyncLog(String username, String ipAddress, String userAgent, String loginStatus, String failureReason) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUsername(username);
            loginLog.setIpAddress(ipAddress);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginStatus(loginStatus);
            loginLog.setFailureReason(failureReason);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setDeviceType(LoginDeviceEnum.WEB.getValue());

            // 解析地理位置
            loginLog.setLocation(parseLocation(ipAddress));

            // 解析User-Agent
            if (CharSequenceUtil.isNotBlank(userAgent)) {
                UserAgent ua = UserAgentUtil.parse(userAgent);
                if (ua != null) {
                    // 解析浏览器
                    String browser = parseBrowser(ua);
                    loginLog.setBrowser(browser);

                    // 解析操作系统
                    String os = parseOs(ua);
                    loginLog.setOs(os);
                } else {
                    setDefaultDeviceInfo(loginLog);
                }
            } else {
                setDefaultDeviceInfo(loginLog);
            }

            loginLogMapper.insert(loginLog);
            log.info("登录日志记录成功: username={}, ip={}, status={}", username, ipAddress, loginStatus);
        } catch (Exception e) {
            log.error("登录日志记录失败: username={}, ip={}, error={}", username, ipAddress, e.getMessage(), e);
        }
    }

    @Override
    public PageResult<LoginLogResult> page(LoginLogQuery query, PageQuery pageQuery) {
        LambdaQueryWrapper<SysLoginLog> queryWrapper = buildQueryWrapper(query);
        
        // 排序：默认按登录时间倒序
        queryWrapper.orderByDesc(SysLoginLog::getLoginTime);

        IPage<SysLoginLog> page = loginLogMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getSize()),
                queryWrapper
        );

        return PageResult.build(page, LoginLogResult.class);
    }

    @Override
    public LoginLogResult detail(Long id) {
        SysLoginLog loginLog = loginLogMapper.selectById(id);
        BizAssert.throwIfNotExists(loginLog, "SysLoginLog", "ID", id);
        return BeanUtil.copyProperties(loginLog, LoginLogResult.class);
    }

    @Override
    public void export(LoginLogQuery query, HttpServletResponse response) {
        LambdaQueryWrapper<SysLoginLog> queryWrapper = buildQueryWrapper(query);
        queryWrapper.orderByDesc(SysLoginLog::getLoginTime);
        
        List<SysLoginLog> list = loginLogMapper.selectList(queryWrapper);
        List<LoginLogExportResult> exportList = BeanUtil.copyToList(list, LoginLogExportResult.class);
        
        ExcelUtils.export(exportList, "登录日志数据", LoginLogExportResult.class, response);
    }

    @Override
    public int cleanExpiredLogs(int retentionDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
        LambdaQueryWrapper<SysLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(SysLoginLog::getLoginTime, expireTime);
        
        int count = loginLogMapper.delete(queryWrapper);
        log.info("清理过期登录日志完成，清理数量: {}, 留存天数: {}", count, retentionDays);
        return count;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysLoginLog> buildQueryWrapper(LoginLogQuery query) {
        LambdaQueryWrapper<SysLoginLog> queryWrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (CharSequenceUtil.isNotBlank(query.getUsername())) {
            queryWrapper.like(SysLoginLog::getUsername, query.getUsername());
        }

        // IP地址模糊查询
        if (CharSequenceUtil.isNotBlank(query.getIpAddress())) {
            queryWrapper.like(SysLoginLog::getIpAddress, query.getIpAddress());
        }

        // 登录状态精确查询
        if (query.getLoginStatus() != null) {
            queryWrapper.eq(SysLoginLog::getLoginStatus, query.getLoginStatus().getValue());
        }

        // 登录时间范围查询
        if (query.getLoginTimeStart() != null) {
            queryWrapper.ge(SysLoginLog::getLoginTime, query.getLoginTimeStart());
        }
        if (query.getLoginTimeEnd() != null) {
            queryWrapper.le(SysLoginLog::getLoginTime, query.getLoginTimeEnd());
        }

        // 租户ID查询（超级管理员可以查询所有租户）
        if (!UserContextHolder.isSuperadmin()) {
            // 非超级管理员，只能查询当前租户
            queryWrapper.eq(SysLoginLog::getTenantId, LoginUtil.getTenantId());
        } else if (query.getTenantId() != null) {
            // 超级管理员指定了租户ID
            queryWrapper.eq(SysLoginLog::getTenantId, query.getTenantId());
        }

        return queryWrapper;
    }

    /**
     * 解析地理位置
     */
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

    /**
     * 解析浏览器
     */
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

    /**
     * 解析操作系统
     */
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


    /**
     * 设置默认设备信息
     */
    private void setDefaultDeviceInfo(SysLoginLog loginLog) {
        loginLog.setBrowser("其他");
        loginLog.setOs("其他");
    }
}
