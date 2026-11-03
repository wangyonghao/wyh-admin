package top.wyhao.web.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {
    private String objectType;  //业务对象类型
    private Long objectId;      //业务对象id
    private String operation;   // 操作类型
    private Long operatorId;    // 操作者id
    private String operatorName;  // 操作者名称
    private String operatorIp;    // 操作者ip
    private LocalDateTime operateTime; // 操作时间
    private Long elapsedMills; // 耗时（毫秒）
    private String status;   // 状态
    private String remark;   // 备注
    private String extra;  // 额外JSON格式数据
}
