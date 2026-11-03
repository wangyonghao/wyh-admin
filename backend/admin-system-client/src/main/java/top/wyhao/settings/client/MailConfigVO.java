package top.wyhao.settings.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 邮件配置（跨模块契约）
 */
@Data
public class MailConfigVO {

    private String host;

    private Integer port;

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String from;

    private boolean sslEnabled;
}
