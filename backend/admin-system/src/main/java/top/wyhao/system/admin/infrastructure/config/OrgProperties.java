package top.wyhao.system.admin.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wyhao-starter.org")
public class OrgProperties {

    /** 邀请链接前缀，末尾不要带 token，例如 http://localhost:5888/org/invite */
    private String inviteBaseUrl = "http://localhost:5888/org/invite";

    private int inviteExpireDays = 7;

    public String getInviteBaseUrl() {
        return inviteBaseUrl;
    }

    public void setInviteBaseUrl(String inviteBaseUrl) {
        this.inviteBaseUrl = inviteBaseUrl;
    }

    public int getInviteExpireDays() {
        return inviteExpireDays;
    }

    public void setInviteExpireDays(int inviteExpireDays) {
        this.inviteExpireDays = inviteExpireDays;
    }
}
