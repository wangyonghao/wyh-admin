package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrgInvite {

    private Long id;
    private Long tenantId;
    private String email;
    private String tokenHash;
    private Integer status;
    private Long invitedBy;
    private LocalDateTime expireTime;
    private Long acceptedUserId;
    private LocalDateTime respondedAt;
    private Long createUser;
    private LocalDateTime createTime;
    private Long updateUser;
    private LocalDateTime updateTime;
    private Integer deleted;
}
