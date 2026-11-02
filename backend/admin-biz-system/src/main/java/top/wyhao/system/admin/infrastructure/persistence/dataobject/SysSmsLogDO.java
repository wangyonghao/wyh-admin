
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.mybatis.model.BaseEntity;
import top.wyhao.cmn.core.enums.ResultStatusEnum;

import java.time.LocalDateTime;

/**
 * 短信日志实体
 */
@Data
@TableName("sys_sms_log")
public class SysSmsLogDO extends BaseEntity {

    @TableId
    private Long id;

    /**
     * 配置 ID
     */
    private Long configId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 参数配置
     */
    private String params;

    /**
     * 发送状态
     */
    private ResultStatusEnum status;

    /**
     * 返回数据
     */
    private String resMsg;

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
}