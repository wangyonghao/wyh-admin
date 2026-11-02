
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysSmsLogDO;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wyhao.system.cmn.sms.SmsClient;
import top.wyhao.system.admin.app.assembler.SmsLogAssembler;
import top.wyhao.system.admin.domain.model.SmsLog;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysSmsLogMapper;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.settings.client.SmsConfigApi;
import top.wyhao.system.admin.app.service.SmsService;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.domain.exception.SmsException;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.util.LinkedHashMap;
import java.util.List;
import top.wyhao.system.admin.app.dto.SmsLogQuery;
import top.wyhao.system.admin.app.dto.SmsLogRequest;
import top.wyhao.system.admin.app.dto.SmsLogResult;

/**
 * 短信日志业务实现
 *
 * @since 2026/05/18
 */
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    private final SmsConfigApi smsConfigApi;
    private final SysSmsLogMapper sysSmsLogMapper;
    private final SmsClient client;
    private final SmsLogAssembler smsLogAssembler;

    @Override
    public void export(SmsLogQuery query, HttpServletResponse response) {
        List<SmsLogResult> list = sysSmsLogMapper.selectObjs(QueryWrapperBuilder.build(query, SysSmsLogDO.class));

        ExcelUtils.export(list, "短信日志.xlsx", SmsLogResult.class, response);
    }

    @Override
    public SmsLogResult get(Long id) {
        SmsLog smsLog = AdminDoMapper.toDomain(sysSmsLogMapper.selectById(id), SmsLog.class);
        if (smsLog == null) {
            throw SmsException.notFound();
        }
        return smsLogAssembler.toResult(smsLog);
    }

    @Override
    public PageResult<SmsLogResult> page(SmsLogQuery query, PageQuery pageQuery) {
        IPage<SysSmsLogDO> resultPage = sysSmsLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getPageSize()),
                QueryWrapperBuilder.build(query, SysSmsLogDO.class));
        return PageResult.of(resultPage).map(item -> smsLogAssembler.toResult(AdminDoMapper.toDomain(item, SmsLog.class)));
    }

    @Override
    public List<SmsLogResult> list(SmsLogQuery query) {
        return List.of();
    }

    @Async
    @Override
    public void logAsync(SmsLogRequest req) {
        SmsLog sysSmsLog = smsLogAssembler.toEntity(req);
        SysSmsLogDO __smsDo = AdminDoMapper.toDo(sysSmsLog, SysSmsLogDO.class);
        sysSmsLogMapper.insert(__smsDo);
        AdminDoMapper.copyBack(__smsDo, sysSmsLog);
    }

    @Override
    public void sendOtp(String phone, OtpScene scene) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", "123456");
        params.put("expire", "5");
        params.put("signature", "签名");
        client.send(phone, getTemplateId(scene), params);
    }

    private String getTemplateId(OtpScene scene) {
        return smsConfigApi.getSmsTemplate(scene.name());
    }
}