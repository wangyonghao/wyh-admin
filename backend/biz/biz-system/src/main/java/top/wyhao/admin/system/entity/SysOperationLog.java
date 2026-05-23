
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志实体
 *

 * @since 2022/12/25 9:11
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId
    private Long id;
    /**
     * 业务对象类型 如：user、sms、login、bug、task、project
     */
    private String objectType;

    /**
     * 业务对象ID
     */
    private Long objectId;

    /**
     * 操作类型 如：login、send、create、update、delete、logout
     */
    private String operation;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 操作者名称
     */
    private String operatorName;

    /**
     * 操作者IP
     */
    private String operatorIp;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 状态 success / fail
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 额外信息 JSON格式
     */
    private String extra;
}
