
package top.wyhao.admin.open.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.db.encrypt.EncryptTypeHandler;
import top.wyhao.cmn.db.model.BaseEntity;

import java.time.LocalDateTime;

/**
 * 应用实体


 * @since 2024/10/17 16:03
 */
@Data
@TableName(value = "sys_app", autoResultMap = true)
public class SysApp extends BaseEntity {

    /**
     * 名称
     */
    private String name;

    /**
     * Access Key（访问密钥）
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String secretKey;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否已过期
     *
     * @return true：已过期；false：未过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }
}