package top.wyhao.cmn.identity;

import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.cmn.core.model.RoleDataScope;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录用户会话模型（基础设施；业务契约见 admin-biz-system-client）
 */
@Data
@NoArgsConstructor
public class LoginUser {

    private Long userId;

    private String username;

    private Long tenantId;

    private Long departmentId;

    private String departmentName;

    private LocalDateTime pwdResetTime;

    private Integer passwordExpirationDays;

    private String deviceType;

    private List<RoleDataScope> roleDataScopes;
}
