package top.wyhao.system.cmn.mail;

import lombok.Data;

/**
 * SMTP 账户。供邮件客户端建连，不是设置接口的请求体。
 */
@Data
public class MailConfig {

    private String host;

    private Integer port;

    private String username;

    private String password;

    private String from;

    private boolean sslEnabled;
}
