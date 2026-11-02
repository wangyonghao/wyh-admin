package top.wyhao.system.cmn.mail;

/**
 * SMTP 账户来源。由持有邮件配置的业务模块实现，邮件客户端不读取配置库。
 */
public interface MailConfigProvider {

    /**
     * @return 当前 SMTP 账户；未配置时由实现决定返回 null 或抛错
     */
    MailConfig getMailConfig();
}
