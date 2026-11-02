
package top.wyhao.system.admin.infrastructure.config.sms;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.proxy.CoreMethodProcessor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.enums.ResultStatusEnum;
import top.wyhao.system.admin.app.service.SmsService;

import java.util.LinkedHashMap;
import java.util.List;
import top.wyhao.system.admin.app.dto.SmsLogRequest;

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
            SmsLogRequest req = new SmsLogRequest(
                    Long.parseLong(result.getConfigId()),
                    param[0].toString(),
                    JSONUtil.toJsonStr(param[1]),
                    result.isSuccess() ? ResultStatusEnum.SUCCESS : ResultStatusEnum.FAILURE,
                    JSONUtil.toJsonStr(result.getData())
            );
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