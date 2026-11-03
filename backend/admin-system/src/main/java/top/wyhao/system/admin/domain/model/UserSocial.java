
package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户社会化关联实体
 *

 * @since 2023/10/11 22:10
 */
@Data
public class UserSocial {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 来源
     */
    private String source;

    /**
     * 开放 ID
     */
    private String openId;

    /**
     * 附加信息
     */
    private String metaJson;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
