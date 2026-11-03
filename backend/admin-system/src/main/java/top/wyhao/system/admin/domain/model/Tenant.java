package top.wyhao.system.admin.domain.model;

import lombok.Data;
import top.wyhao.cmn.core.enums.StatusEnum;

import java.time.LocalDateTime;

/**
 * 租户实体。对应表 {@code tenant}（见 Liquibase platform-schema）。
 */
@Data
public class Tenant {

    private Long id;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 域名
     */
    private String domain;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private StatusEnum status;

    /**
     * 套餐 ID
     */
    private Long packageId;

    /**
     * 管理员用户
     */
    private Long adminUser;

    /**
     * 管理员用户名
     */
    private String adminUsername;
}
