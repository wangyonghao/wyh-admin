package top.wyhao.identity.client;

import lombok.Data;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.cmn.core.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户的资料快照，字段与用户详情响应一致。
 */
@Data
public class UserProfile {

    private Long id;
    private Long createUser;
    private String createUserString;
    private LocalDateTime createTime;
    private Boolean disabled;
    private Long updateUser;
    private String updateUserString;
    private LocalDateTime updateTime;
    private String username;
    private String displayName;
    private StatusEnum status;
    private GenderEnum gender;
    private Long departmentId;
    private String departmentName;
    private List<Long> roleIds;
    private List<String> roleNames;
    private String phone;
    private String email;
    private Boolean isBuiltin;
    private String description;
    private Long avatar;
    private LocalDateTime pwdResetTime;
}
