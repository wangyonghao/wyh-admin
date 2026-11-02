package top.wyhao.settings.client;

import top.wyhao.system.cmn.sms.SmsConfig;

/**
 * 短信通道与模板配置。由 notification 模块基于 {@link ConfigStoreApi} 实现。
 */
public interface SmsConfigApi {

    SmsConfig getSmsConfig();

    void updateSmsConfig(Object config);

    String getSmsTemplate(String scene);
}
