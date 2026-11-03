package top.wyhao.system.admin.domain.model;

import lombok.Data;
import top.wyhao.tenant.core.TenantedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @since 2026/4/14
 */
@Data
public class User implements TenantedEntity {

    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 显示名称
     */
    private String displayName;
    /**
     * 密码
     */
    private String password;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;
    /**
     * 头像文件 ID（关联 file.id）
     */
    private Long avatar;
    /**
     * 描述
     */
    private String description;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 是否为系统内置数据
     */
    private Boolean isBuiltin;
    /**
     * 最近改密时间
     */
    private LocalDateTime pwdUpdateTime;
    /**
     * 密码过期日
     */
    private LocalDate pwdExpireDate;
    /**
     * 曾用密码哈希（{@code |} 拼接，最多 {@link PasswordHistory#MAX_SIZE} 个，新在前）
     */
    private String passwordHistory;
    /**
     * 部门 ID
     */
    private Long departmentId;
    /**
     * 租户 ID。行级隔离列，与当前登录租户一致。
     */
    private Long tenantId;
    /**
     * 创建人
     */
    private Long createUser;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改人
     */
    private Long updateUser;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
