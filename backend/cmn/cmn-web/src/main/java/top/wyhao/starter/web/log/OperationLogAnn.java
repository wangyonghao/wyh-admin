
package top.wyhao.starter.web.log;

import java.lang.annotation.*;

/**
 * 日志注解
 * <p>用于接口方法或类上，辅助 Spring Doc 使用效果最佳</p>
 *

 * @since 1.1.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLogAnn {
    // 业务对象类型: user/order/task/bug
    String objectType();
    // 操作类型：create/update/delete
    String operationType();
    // 业务ID参数名（从入参拿id）
    String objectIdParam() default "id";
    // 备注
    String remark() default "";
}
