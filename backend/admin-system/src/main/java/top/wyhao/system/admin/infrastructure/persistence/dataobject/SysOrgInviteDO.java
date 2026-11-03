package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_org_invite")
public class SysOrgInviteDO {

    @TableId
    private Long id;
    private Long tenantId;
    private String email;
    private String tokenHash;
    private Integer status;
    private Long invitedBy;
    private LocalDateTime expireTime;
    private Long acceptedUserId;
    private LocalDateTime respondedAt;
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    private Integer deleted;
}
