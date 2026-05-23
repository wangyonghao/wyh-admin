
package top.wyhao.starter.web.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 键值对响应参数
 *
 * @param <T>

 * @since 2.1.0
 */
@Schema(description = "键值对响应参数")
public class LabelValueResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签
     */
    @Schema(description = "标签", example = "男")
    private String label;

    /**
     * 值
     */
    @Schema(description = "值", example = "1")
    private T value;

    /**
     * 是否禁用
     */
    @Schema(description = "是否禁用", example = "false")
    private Boolean disabled;

    /**
     * 额外数据
     */
    @Schema(description = "额外数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object extra;

    public LabelValueResult() {
    }

    public LabelValueResult(String label, T value) {
        this.label = label;
        this.value = value;
    }

    public LabelValueResult(String label, T value, Object extra) {
        this.label = label;
        this.value = value;
        this.extra = extra;
    }

    public LabelValueResult(String label, T value, Boolean disabled) {
        this.label = label;
        this.value = value;
        this.disabled = disabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
