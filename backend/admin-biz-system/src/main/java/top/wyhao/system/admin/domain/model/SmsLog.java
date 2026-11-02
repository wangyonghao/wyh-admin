
package top.wyhao.system.admin.domain.model;

import lombok.Data;
import top.wyhao.cmn.core.enums.ResultStatusEnum;

import java.time.LocalDateTime;

/**
 * 短信日志实体
 */
@Data
public class SmsLog {

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
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}