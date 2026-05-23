
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.db.encrypt.EncryptTypeHandler;
import top.wyhao.cmn.db.model.BaseEntity;
import top.wyhao.starter.core.enums.StatusEnum;

import java.time.LocalDateTime;

/**
 * 短信配置实体
 *


 * @since 2025/03/15 18:41
 */
@Data
@TableName(value = "sys_sms_config", autoResultMap = true)
public class SysSmsConfig extends BaseEntity {
    @TableId
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 厂商
     */
    private String supplier;

    /**
     * Access Key
     */
    private String accessKey;

    /**
     * Secret Key
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String secretKey;

    /**
     * 短信签名
     */
    private String signature;

    /**
     * 模板 ID
     */
    private String templateId;

    /**
     * 负载均衡权重
     */
    private Integer weight;

    /**
     * 重试间隔（单位：秒）
     */
    private Integer retryInterval;

    /**
     * 重试次数
     */
    private Integer maxRetries;

    /**
     * 发送上限
     */
    private Integer maximum;

    /**
     * 各个厂商独立配置
     */
    private String supplierConfig;

    /**
     * 是否为默认存储
     */
    private Boolean isDefault;

    /**
     * 状态
     */
    private StatusEnum status;

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