
package top.wyhao.admin.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.db.model.BaseEntity;
import top.wyhao.starter.core.enums.StatusEnum;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 租户实体
 *


 * @since 2024/11/26 17:20
 */
@Data
@TableName("tenant")
public class Tenant extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 管理员用户
     */
    private Long adminUser;

    /**
     * 管理员用户名
     */
    private String adminUsername;

    /**
     * 套餐 ID
     */
    private Long packageId;
}