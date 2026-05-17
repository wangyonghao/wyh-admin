package top.wyhao.admin.system.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.admin.system.mapper.ConfigMapper;
import top.wyhao.starter.core.model.MailConfig;
import top.wyhao.starter.core.spi.MailConfigProvider;

@Component
@RequiredArgsConstructor
public class MailConfigProviderImpl implements MailConfigProvider {
    private final ConfigMapper configMapper;

    @Override
    public MailConfig getMailConfig() {
        return configMapper.getConfig("mail", MailConfig.class);
    }
}
