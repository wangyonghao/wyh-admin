package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import top.wyhao.system.admin.domain.model.MembershipStatus;

import top.wyhao.system.admin.domain.model.MembershipScopeType;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成员关系（用户-部门 / 用户-角色 / 用户-组织）。
 */
@Data
@NoArgsConstructor
@TableName("sys_membership")
public class SysMembershipDO {

    @TableId
    private Long id;

    private Long userId;

    private MembershipScopeType scopeType;

    private Long scopeId;

    private Boolean isPrimary;

    private Integer status;

    private LocalDateTime joinedAt;

    private LocalDateTime expiredAt;

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
