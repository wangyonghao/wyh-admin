package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrgApplication {

    private Long id;
    private Long tenantId;
    private Long userId;
    private String message;
    private Integer status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private Long createUser;
    private LocalDateTime createTime;
    private Long updateUser;
    private LocalDateTime updateTime;
    private Integer deleted;
}
