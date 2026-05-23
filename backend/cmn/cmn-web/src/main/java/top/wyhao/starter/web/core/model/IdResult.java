
package top.wyhao.starter.web.core.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * ID 响应参数
 *

 * @since 2.5.0
 */
public class IdResult<T> {

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private T id;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public IdResult() {
    }

    public IdResult(final T id) {
        this.id = id;
    }
}
