
package top.wyhao.cmn.core.exception;

import lombok.Data;
import top.wyhao.cmn.core.util.I18nUtils;

/**
 * 业务异常基类
 *

 */
@Data
public class BizException extends RuntimeException {
    /**
     * 错误码
     */
    private String code;
    /**
     * 错误码对应的参数
     */
    private Object[] args;
    /**
     * 错误消息
     */
    private String defaultMessage;


    public BizException(String message) {
        this(null, null, message);
    }

    public BizException(String code, Object[] args) {
        this( code, args, null);
    }

    public BizException(String code, String defaultMessage) {
        this(code, null, defaultMessage);
    }

    public BizException(String code, Object[] args, String defaultMessage) {
        super(defaultMessage);
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }


    // 性能优化：避免重复创建栈轨迹
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }


    @Override
    public String getMessage() {
        if (code != null) {
            return I18nUtils.message(code, args);
        }
        return defaultMessage;
    }

    public static BizException of(String code){
        return of(code, null);
    }

    public static BizException of(String code, String defaultMessage){
        return of(code, null, defaultMessage);
    }

    public static BizException of(String code, Object[] args, String defaultMessage){
        BizException ex = new BizException(code, args, defaultMessage);
        ex.setCode(code);
        ex.setArgs(args);
        ex.setDefaultMessage(defaultMessage);
        return ex;
    }
}