package top.wyhao.system.admin.domain.model;

import java.time.LocalDateTime;


import lombok.Data;

/**
 * 定时任务定义
 */
@Data
public class Job {

    private Long id;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

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
