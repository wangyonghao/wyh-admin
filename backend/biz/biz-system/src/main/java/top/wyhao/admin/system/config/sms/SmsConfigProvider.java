
package top.wyhao.admin.system.config.sms;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;
import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;
import org.springframework.stereotype.Component;
import top.wyhao.admin.cmn.sms.SmsConfig;
import top.wyhao.admin.system.service.ConfigService;

import java.util.List;

/**
 * 短信配置读取-数据源实现
 *
 * @since 2025/03/15 22:15
 */
@Component
@RequiredArgsConstructor
public class SmsConfigProvider implements SmsReadConfig {

    private final ConfigService configService;

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        SmsConfig smsConfig = configService.getSmsConfig();
        return from(smsConfig);
    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        SmsConfig smsConfig = configService.getSmsConfig();
        if(smsConfig == null){
            return List.of();
        }
        return List.of(from(smsConfig));
    }


    /**
     * 将实体配置转换为 SMS4J 配置
     *
     * @param smsConfig 实体配置对象
     * @return SMS4J 配置基类
     */
    public static BaseConfig from(SmsConfig smsConfig) {
        if (smsConfig == null) {
            return null;
        }
        String supplier = smsConfig.getSupplier();
        BaseProviderFactory<?, ?> providerFactory = ProviderFactoryHolder.requireForSupplier(supplier);
        if (providerFactory == null) {
            return null;
        }
        return (BaseConfig) BeanUtil.toBean(smsConfig, providerFactory.getConfigClass());
    }
}