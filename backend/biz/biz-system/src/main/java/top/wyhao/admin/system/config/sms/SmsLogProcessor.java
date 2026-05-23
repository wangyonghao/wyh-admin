
package top.wyhao.admin.system.config.sms;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.proxy.CoreMethodProcessor;
import org.springframework.stereotype.Component;
import top.wyhao.starter.core.enums.ResultStatusEnum;
import top.wyhao.admin.system.model.bo.SmsLogRequest;
import top.wyhao.admin.system.service.SmsService;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 短信日志处理器
 *


 * @since 2025/03/15 22:15
 */
@Component
@RequiredArgsConstructor
public class SmsLogProcessor implements CoreMethodProcessor {

    private final SmsService smsService;

    @Override
    public Object postProcessor(SmsResponse result, Object[] param) {
        if (NumberUtil.isNumber(result.getConfigId())) {
            SmsLogRequest req = new SmsLogRequest();
            req.setConfigId(Long.parseLong(result.getConfigId()));
            req.setPhone(param[0].toString());
            req.setParams(JSONUtil.toJsonStr(param[1]));
            req.setStatus(result.isSuccess() ? ResultStatusEnum.SUCCESS : ResultStatusEnum.FAILURE);
            req.setResMsg(JSONUtil.toJsonStr(result.getData()));
            smsService.logAsync(req);
        }
        return CoreMethodProcessor.super.postProcessor(result, param);
    }

    @Override
    public void sendMessagePreProcess(String phone, Object message) {
        // do nothing
    }

    @Override
    public void sendMessageByTemplatePreProcess(String phone,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        // do nothing
    }

    @Override
    public void massTextingPreProcess(List<String> phones, String message) {
        // do nothing
    }

    @Override
    public void massTextingByTemplatePreProcess(List<String> phones,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        // do nothing
    }
}