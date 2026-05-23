package top.wyhao.admin.cmn.sms;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.callback.CallBack;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 短信客户端
 *
 *
 *

 * @since 2026/5/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsClient {

    /**
     * 发送短信
     * @param phone 接收手机号
     * @param templateId 模板id
     * @param params 模板参数
     * @return 是否发送成功
     */
    public boolean send(String phone, String templateId, LinkedHashMap<String, String> params){
        if (StrUtil.isBlank(phone)) {
            throw SmsException.recipientIsRequired();
        }
        if (StrUtil.isBlank(templateId)) {
            throw SmsException.contentIsRequired();
        }

        SmsBlend smsBlend = getSmsBlend();
        SmsResponse response = smsBlend.sendMessage(phone, templateId, params);
        return response != null && response.isSuccess();
    }

    /**
     * 异步发送短信
     * @param phone 接收手机号
     * @param templateId 模板id
     * @param params 模板参数
     * @param callBack 回调函数，用于接收短信的发送结果
     */
    public void sendAsync(String phone, String templateId, LinkedHashMap<String, String> params, CallBack callBack){
        if (StrUtil.isBlank(phone)) {
            throw SmsException.recipientIsRequired();
        }
        if (StrUtil.isBlank(templateId)) {
            throw SmsException.contentIsRequired();
        }

        SmsBlend smsBlend = getSmsBlend();
        smsBlend.sendMessageAsync(phone, templateId, params, callBack);
    }


    /**
     * 发送通知短信
     *
     * @param phones  手机号列表
     * @param content 短信内容
     */
    public void massSend(Collection<String> phones, String content, CallBack callBack) {
        if (CollectionUtil.isEmpty(phones)) {
            throw SmsException.recipientIsRequired();
        }
        if (StrUtil.isBlank(content)) {
            throw SmsException.contentIsRequired();
        }
        try {
            SmsBlend smsBlend = getSmsBlend();
            for (String phone : phones) {
                smsBlend.sendMessageAsync(phone, content, callBack);
            }
            log.info("短信通知发送完成，接收人数：{}", phones.size());
        } catch (Exception e) {
            log.error("短信通知发送失败", e);
            throw SmsException.sendingFailed(e);
        }
    }

    /**
     * 群发短信
     * 异步
     *
     * @param phones 手机号列表
     * @param params 模板参数
     */
    public void massSend(List<String> phones, String templateId, LinkedHashMap<String, String> params, CallBack callBack) {
        if (CollectionUtil.isEmpty(phones)) {
            throw SmsException.recipientIsRequired();
        }
        if (CollectionUtil.isEmpty(params)) {
            throw SmsException.contentIsRequired();
        }
        try {
            SmsBlend smsBlend = getSmsBlend();
            for (String phone : phones){
                smsBlend.sendMessageAsync(phone, templateId, params, callBack);
            }
            log.info("短信发送完成，接收人数：{}", phones.size());
        } catch (Exception e) {
            log.error("短信发送失败", e);
            throw SmsException.sendingFailed(e);
        }
    }

    /**
     * 获取 SMS Blend 实例
     */
    private SmsBlend getSmsBlend() {
        SmsBlend smsBlend = SmsFactory.getSmsBlend();
        if (smsBlend == null) {
            throw SmsException.configNotFound();
        }
        return smsBlend;
    }
    public static void register(String configId){
        SmsReadConfig smsReadConfig = SpringUtil.getBean(SmsReadConfig.class);
        SmsFactory.createSmsBlend(smsReadConfig, configId);
    }
    public static void unregister(String configId){
        SmsFactory.unregister(configId);
    }
}
