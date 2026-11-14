package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.mybatis.encrypt.EncryptTypeHandler;

import java.time.LocalDateTime;

/**
 * 待验证邮箱注册。邮箱与用户表使用同一套 AES TypeHandler。
 */
@Data
@TableName(value = "sys_email_registration", autoResultMap = true)
public class SysEmailRegistrationDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    @TableField(typeHandler = EncryptTypeHandler.class)
    private String email;

    private String username;

    private String passwordHash;

    private String tokenHash;

    private Long departmentId;

    private Long roleId;

    private LocalDateTime expiresAt;

    private LocalDateTime sentAt;

    private String sendStatus;

    private LocalDateTime consumedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
