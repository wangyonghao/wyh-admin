package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.mybatis.model.BaseEntity;

import java.time.LocalDateTime;

/**
 * 租户实体。对应表 {@code tenant}（见 Liquibase platform-schema）。
 */
@Data
@TableName("tenant")
public class TenantDO extends BaseEntity {

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
