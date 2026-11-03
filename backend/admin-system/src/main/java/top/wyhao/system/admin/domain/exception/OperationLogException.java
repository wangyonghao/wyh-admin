package top.wyhao.system.admin.domain.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 操作日志业务异常
 */
public class OperationLogException extends BizException {

    public OperationLogException(String message) {
        super(message);
    }

    public OperationLogException(String code, String message) {
        super(code, message);
    }

    public static OperationLogException of(String message) {
        return new OperationLogException(message);
    }

    public static OperationLogException of(String code, String message) {
        return new OperationLogException(code, message);
    }

    public static OperationLogException notFound() {
        return of("OPERATIONLOG_NOT_FOUND", "操作日志不存在");
    }
}
