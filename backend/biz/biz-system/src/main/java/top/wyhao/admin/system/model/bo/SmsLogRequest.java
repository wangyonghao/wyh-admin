
package top.wyhao.admin.system.model.bo;

import lombok.Data;
import top.wyhao.starter.core.enums.ResultStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信日志创建或修改请求参数
 *


 * @since 2025/03/15 22:15
 */
@Data
public class SmsLogRequest {
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
}