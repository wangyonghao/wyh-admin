package top.wyhao.starter.quartz.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 声明一个可被管理端选择的定时任务。
 *
 * <p>{@code code} 是稳定主键，管理端只允许选择已注册的 code，禁止按类名反射执行。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface JobHandler {

    /**
     * 稳定编码，例如 {@code noticePublish}
     */
    String code();

    /**
     * 界面展示名称
     */
    String name();

    /**
     * 给非技术人员看的说明
     */
    String description() default "";

    /**
     * 是否允许同一任务实例重叠执行。默认不允许。
     */
    boolean allowConcurrent() default false;
}
