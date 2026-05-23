
package top.wyhao.starter.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 邮件配置
 *

 * @since 2024/4/26
 */
@Data
public class MailConfig {
    /**
     * SMTP服务器地址
     */
    private String host;

    /**
     * SMTP端口
     */
    private Integer port;

    /**
     * 发件人邮箱
     */
    private String username;

    /**
     * 邮箱密码/授权码（敏感字段）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 发件人名称
     */
    private String from;

    /**
     * 是否启用SSL
     */
    private boolean sslEnabled;
}
