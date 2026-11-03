package top.wyhao.web.log;

import lombok.Data;

@Data
public class OperationLogDetail {
    private Long logId;
    private String fieldName;
    private String fieldComment;
    private String oldValue;
    private String newValue;
}
