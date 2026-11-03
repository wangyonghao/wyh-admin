package top.wyhao.system.admin.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.cmn.core.enums.ResultStatusEnum;

/**
 * 短信日志创建或修改请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsLogRequest {



    private Long configId;
    private String phone;
    private String params;
    private ResultStatusEnum status;
    private String resMsg;
}
