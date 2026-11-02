package top.wyhao.system.admin.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wyhao.system.admin.app.service.JobAdminService;

/**
 * 启动时把 sys_job 与集群 JobStore 对齐，避免某实例漏注册。
 */
@Slf4j
@Component
@Order(200)
@RequiredArgsConstructor
public class JobScheduleReconcileRunner implements ApplicationRunner {

    private final JobAdminService jobAdminService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jobAdminService.reconcile();
        } catch (Exception e) {
            log.error("对齐定时任务调度失败", e);
        }
    }
}
