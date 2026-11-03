package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.SmsLogQuery;
import top.wyhao.system.admin.app.dto.SmsLogRequest;
import top.wyhao.system.admin.app.dto.SmsLogResult;
import top.wyhao.web.core.model.PageQuery;

import java.util.List;

/**
 * 短信日志 Service
 *
 * @since 2025/03/15 22:15
 */
public interface SmsService {
    void export(@Valid SmsLogQuery query, HttpServletResponse response);

    SmsLogResult get(Long id);

    PageResult<SmsLogResult> page(@Valid SmsLogQuery query, @Valid PageQuery pageQuery);

    List<SmsLogResult> list(@Valid SmsLogQuery query);

    void logAsync(SmsLogRequest req);
}
