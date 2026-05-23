
package top.wyhao.admin.system.config.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.core.proxy.SmsProxyFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 短信配置加载器
 *


 * @since 2025/03/15 22:15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsConfigLoader implements ApplicationRunner {

    private final SmsConfigProvider smsReadConfig;
    private final SmsLogProcessor smsLogProcessor;

    @Override
    public void run(ApplicationArguments args) {
        if(smsReadConfig.getSupplierConfigList().isEmpty()){
            log.warn("[cmn-sms]从数据库中加载短信配置失败，短信服务暂不可用，请前往管理台进行配置");
            return;
        }
        SmsFactory.createSmsBlend(smsReadConfig);
        SmsProxyFactory.addPreProcessor(smsLogProcessor);
        log.debug("[cmn-sms]短信初始化完成");
    }
}
