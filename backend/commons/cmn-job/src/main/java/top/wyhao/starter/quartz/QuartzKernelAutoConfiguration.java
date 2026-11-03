package top.wyhao.starter.quartz;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import top.wyhao.starter.quartz.spi.JobExecutionRecorder;
import top.wyhao.starter.quartz.spi.JobTask;

/**
 * Quartz 内核自动配置：注册表、分发监听、集群 Scheduler 定制。
 */
@AutoConfiguration
@AutoConfigureBefore(QuartzAutoConfiguration.class)
@ConditionalOnClass(SchedulerFactoryBean.class)
public class QuartzKernelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JobHandlerRegistry jobHandlerRegistry(ObjectProvider<JobTask> tasks) {
        return new JobHandlerRegistry(tasks.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    public JobExecutionListener jobExecutionListener(ObjectProvider<JobExecutionRecorder> recorder) {
        return new JobExecutionListener(recorder);
    }

    @Bean
    public SchedulerFactoryBeanCustomizer wyhQuartzCustomizer(JobExecutionListener jobExecutionListener) {
        return factory -> {
            factory.setOverwriteExistingJobs(true);
            factory.setWaitForJobsToCompleteOnShutdown(true);
            factory.setGlobalJobListeners(jobExecutionListener);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public QuartzJobScheduler quartzJobScheduler(
            org.quartz.Scheduler scheduler,
            JobHandlerRegistry jobHandlerRegistry) {
        return new QuartzJobScheduler(scheduler, jobHandlerRegistry);
    }

    /**
     * JDBC JobStore 依赖 QRTZ_* 表，必须等 Liquibase 先建表再启动 Scheduler。
     */
    @Bean
    public static BeanFactoryPostProcessor quartzDependsOnLiquibase() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("liquibase")
                    && beanFactory.containsBeanDefinition("quartzScheduler")) {
                beanFactory.getBeanDefinition("quartzScheduler").setDependsOn("liquibase");
            }
        };
    }
}
