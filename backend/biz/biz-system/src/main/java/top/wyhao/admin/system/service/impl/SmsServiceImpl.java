
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wyhao.admin.cmn.sms.SmsClient;
import top.wyhao.admin.system.entity.SysSmsLog;
import top.wyhao.admin.system.mapper.SysSmsLogMapper;
import top.wyhao.admin.system.model.bo.SmsLogRequest;
import top.wyhao.admin.system.model.query.SmsLogQuery;
import top.wyhao.admin.system.model.vo.SmsLogResult;
import top.wyhao.admin.system.otp.enums.OtpScene;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.admin.system.service.SmsService;
import top.wyhao.cmn.db.util.QueryWrapperUtil;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信日志业务实现
 *

 * @since 2026/05/18
 */
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    private final ConfigService configService;
    private final SysSmsLogMapper sysSmsLogMapper;
    private final SmsClient client;

    @Override
    public void export(SmsLogQuery query, HttpServletResponse response) {
        QueryWrapper<SysSmsLog> queryWrapper = QueryWrapperUtil.build(query);
        List<SmsLogResult> list = sysSmsLogMapper.selectObjs(queryWrapper);

        ExcelUtils.export(list, "短信日志.xlsx", SmsLogResult.class, response);
    }

    @Override
    public SmsLogResult get(Long id) {
        SysSmsLog smsLog = sysSmsLogMapper.selectById(id);
        if (smsLog == null) {
           throw new BusinessException("记录不存在");
        }
        return BeanUtil.toBean(smsLog, SmsLogResult.class);
    }

    @Override
    public PageResult<SmsLogResult> page(SmsLogQuery query, PageQuery pageQuery) {
        QueryWrapper<SysSmsLog> queryWrapper = QueryWrapperUtil.build(query);
        IPage<SysSmsLog> resultPage = sysSmsLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        return PageResult.build(resultPage, SmsLogResult.class);
    }

    @Override
    public List<SmsLogResult> list(SmsLogQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Async
    @Override
    public void logAsync(SmsLogRequest req) {
        SysSmsLog sysSmsLog = new SysSmsLog();
        BeanUtil.copyProperties(req, sysSmsLog);
        sysSmsLogMapper.insert(sysSmsLog);
    }

    @Override
    public void sendOtp(String phone, OtpScene scene) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", "123456");
        params.put("expire", "5");
        params.put("signature", "签名");
        client.send(phone, getTemplateId(scene), params);
    }

    private String getTemplateId(OtpScene scene){
        return configService.getSmsTemplate(scene.name());
    }
}