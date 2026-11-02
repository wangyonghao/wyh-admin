
package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.util.List;
import top.wyhao.system.admin.app.dto.SmsLogQuery;
import top.wyhao.system.admin.app.dto.SmsLogRequest;
import top.wyhao.system.admin.app.dto.SmsLogResult;

/**
 * 短信 Service
 *

 * @since 2025/03/15 22:15
 */
public interface SmsService {
    void export(@Valid SmsLogQuery query, HttpServletResponse response);

    SmsLogResult get(Long id);

    PageResult<SmsLogResult> page(@Valid SmsLogQuery query, @Valid PageQuery pageQuery);

    List<SmsLogResult> list(@Valid SmsLogQuery query);

    void logAsync(SmsLogRequest req);

    void sendOtp(String phone, OtpScene scene);
}