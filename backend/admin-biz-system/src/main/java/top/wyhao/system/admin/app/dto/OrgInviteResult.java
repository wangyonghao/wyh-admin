package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "组织邀请")
public class OrgInviteResult {
    private Long id;
    private Long tenantId;
    private String orgName;
    private String orgCode;
    private String email;
    private Integer status;
    private LocalDateTime expireTime;
    /** 仅创建邀请时返回，用于拼邮件链接；列表查询不返回明文 token */
    private String token;
}
