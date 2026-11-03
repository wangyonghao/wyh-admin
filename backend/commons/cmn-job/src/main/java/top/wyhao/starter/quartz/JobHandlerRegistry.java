package top.wyhao.starter.quartz;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import top.wyhao.cmn.core.exception.BizException;
import top.wyhao.starter.quartz.annotation.JobHandler;
import top.wyhao.starter.quartz.spi.JobHandlerDescriptor;
import top.wyhao.starter.quartz.spi.JobTask;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描 {@link JobHandler} 声明的任务实现。
 */
public class JobHandlerRegistry implements InitializingBean {

    private final List<JobTask> tasks;
    private final Map<String, Registration> registrations = new LinkedHashMap<>();

    public JobHandlerRegistry(List<JobTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void afterPropertiesSet() {
        for (JobTask task : tasks) {
            Class<?> targetClass = AopUtils.getTargetClass(task);
            JobHandler annotation = targetClass.getAnnotation(JobHandler.class);
            if (annotation == null) {
                throw new IllegalStateException("JobTask 缺少 @JobHandler: " + targetClass.getName());
            }
            if (registrations.containsKey(annotation.code())) {
                throw new IllegalStateException("重复的任务编码: " + annotation.code());
            }
            registrations.put(annotation.code(), new Registration(annotation, task));
        }
    }

    public JobTask requireTask(String code) {
        Registration registration = registrations.get(code);
        if (registration == null) {
            throw new BizException("未注册的任务: " + code);
        }
        return registration.task();
    }

    public JobHandlerDescriptor requireDescriptor(String code) {
        Registration registration = registrations.get(code);
        if (registration == null) {
            throw new BizException("未注册的任务: " + code);
        }
        return toDescriptor(registration.annotation());
    }

    public Collection<JobHandlerDescriptor> list() {
        return registrations.values().stream().map(item -> toDescriptor(item.annotation())).toList();
    }

    public boolean contains(String code) {
        return registrations.containsKey(code);
    }

    private static JobHandlerDescriptor toDescriptor(JobHandler annotation) {
        return new JobHandlerDescriptor(
                annotation.code(),
                annotation.name(),
                annotation.description(),
                annotation.allowConcurrent());
    }

    private record Registration(JobHandler annotation, JobTask task) {
    }
}
