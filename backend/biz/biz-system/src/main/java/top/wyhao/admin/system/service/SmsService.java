
package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.admin.system.model.bo.SmsLogRequest;
import top.wyhao.admin.system.model.query.SmsLogQuery;
import top.wyhao.admin.system.model.vo.SmsLogResult;
import top.wyhao.admin.system.otp.enums.OtpScene;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 短信 Service
 *

 * @since 2025/03/15 22:15
 */
public interface SmsService {
    void export(@Valid SmsLogQuery query, HttpServletResponse response);

    SmsLogResult get(Long id);

    PageResult<SmsLogResult> page(@Valid SmsLogQuery query, @Valid PageQuery pageQuery);

    List<SmsLogResult> list(@Valid SmsLogQuery query, @Valid SortQuery sortQuery);

    void logAsync(SmsLogRequest req);

    void sendOtp(String phone, OtpScene scene);
}