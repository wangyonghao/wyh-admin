package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wyhao.mybatis.model.BaseEntity;

/**
 * 定时任务定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job")
public class SysJobDO extends BaseEntity {

    private String name;
    private String handlerCode;
    private String cron;
    private String scheduleMode;
    private String schedulePayload;
    private String scheduleLabel;
    private String params;
    /** 0 停止 1 激活 */
    private Integer status;
    private String remark;
}
