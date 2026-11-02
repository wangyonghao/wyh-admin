package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.mybatis.encrypt.EncryptTypeHandler;
import top.wyhao.tenant.core.TenantedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @since 2026/4/14
 */
@Data
@TableName(value = "sys_user", autoResultMap = true)
public class SysUserDO implements TenantedEntity {

    @TableId
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
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, typeHandler = EncryptTypeHandler.class)
    private String email;

    /**
     * 手机号码
     */
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, typeHandler = EncryptTypeHandler.class)
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
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
