
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import top.wyhao.cmn.db.encrypt.EncryptTypeHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 *

 * @since 2026/4/14
 */
@Data
@TableName(value = "sys_user", autoResultMap = true)
public class SysUser {
    @TableId
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickname;
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
     * 头像地址
     */
    private String avatar;
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
     * 部门 ID
     */
    private Long deptId;
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
