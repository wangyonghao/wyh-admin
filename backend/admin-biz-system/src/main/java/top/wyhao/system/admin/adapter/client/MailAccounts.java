package top.wyhao.system.admin.adapter.client;

import top.wyhao.system.cmn.mail.MailConfig;
import top.wyhao.settings.client.MailConfigVO;

/**
 * 设置接口上的邮件配置与 SMTP 账户之间的映射。
 */
public final class MailAccounts {

    private MailAccounts() {
    }

    public static MailConfig from(MailConfigVO vo) {
        if (vo == null) {
            return null;
        }
        MailConfig config = new MailConfig();
        config.setHost(vo.getHost());
        config.setPort(vo.getPort());
        config.setUsername(vo.getUsername());
        config.setPassword(vo.getPassword());
        config.setFrom(vo.getFrom());
        config.setSslEnabled(vo.isSslEnabled());
        return config;
    }
}
