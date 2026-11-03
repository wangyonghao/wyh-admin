package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.system.cmn.mail.MailConfig;
import top.wyhao.system.cmn.mail.MailConfigProvider;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;
import top.wyhao.settings.client.MailConfigVO;

@Component
@RequiredArgsConstructor
public class MailConfigProviderImpl implements MailConfigProvider {

    private final ConfigStoreApi configStoreApi;

    @Override
    public MailConfig getMailConfig() {
        return MailAccounts.from(configStoreApi.get(ConfigKeys.MAIL, MailConfigVO.class));
    }
}
