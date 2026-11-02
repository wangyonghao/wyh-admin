package top.wyhao.system.auth.app.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.system.auth.app.assembler.AuthenticationLogAssembler;
import top.wyhao.system.auth.app.dto.AuthenticationLogExcelResult;
import top.wyhao.system.auth.app.dto.AuthenticationLogQuery;
import top.wyhao.system.auth.app.dto.AuthenticationLogResult;
import top.wyhao.system.auth.app.service.AuthLogService;
import top.wyhao.system.auth.domain.authlog.AuthLog;
import top.wyhao.system.auth.domain.authlog.AuthLogCriteria;
import top.wyhao.system.auth.domain.authlog.AuthLogDomainService;
import top.wyhao.web.core.model.PageQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志 Service 实现（仅查询 / 导出 / 清理）
 *
 * @since 2026/05/08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLogServiceImpl implements AuthLogService {

    private final AuthLogDomainService authLogDomainService;
    private final AuthenticationLogAssembler authenticationLogAssembler;

    @Override
    public PageResult<AuthenticationLogResult> page(AuthenticationLogQuery query, PageQuery pageQuery) {
        return authLogDomainService.page(toCriteria(query), pageQuery.getPage(), pageQuery.getPageSize())
                .map(authenticationLogAssembler::toResult);
    }

    @Override
    public void export(AuthenticationLogQuery query, HttpServletResponse response) {
        List<AuthLog> list = authLogDomainService.list(toCriteria(query));
        List<AuthenticationLogExcelResult> exportList = authenticationLogAssembler.toExcelList(list);
        ExcelUtils.export(exportList, "登录日志数据", AuthenticationLogExcelResult.class, response);
    }

    @Override
    public int cleanExpiredLogs(int retentionDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
        int count = authLogDomainService.deleteBefore(expireTime);
        log.info("清理过期登录日志完成，清理数量: {}, 留存天数: {}", count, retentionDays);
        return count;
    }

    private AuthLogCriteria toCriteria(AuthenticationLogQuery query) {
        AuthLogCriteria criteria = new AuthLogCriteria();
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
}
