
package top.wyhao.starter.web.json.exception;

import top.wyhao.starter.core.exception.SystemException;


/**
 * JSON 异常
 *

 * @since 2.13.2
 */
public class JSONException extends SystemException {
    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
