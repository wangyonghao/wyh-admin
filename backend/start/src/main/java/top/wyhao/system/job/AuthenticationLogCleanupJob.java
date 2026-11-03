package top.wyhao.system.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import top.wyhao.starter.quartz.annotation.JobHandler;
import top.wyhao.starter.quartz.spi.JobContext;
import top.wyhao.starter.quartz.spi.JobTask;
import top.wyhao.system.admin.app.service.LoginLogService;

/**
 * 登录日志清理
 */
@Slf4j
@JobHandler(code = "loginLogCleanup", name = "登录日志清理", description = "按保留天数删除过期登录日志")
@RequiredArgsConstructor
public class AuthenticationLogCleanupJob implements JobTask {

    private final LoginLogService loginLogService;

    @Value("${system.login-log.retention-days:365}")
    private int retentionDays;

    @Value("${system.login-log.auto-cleanup-enabled:true}")
    private boolean autoCleanupEnabled;

    @Override
    public void execute(JobContext context) {
        if (!autoCleanupEnabled) {
            log.info("定时任务 [登录日志清理] 已禁用，跳过执行");
            return;
        }
        log.info("定时任务 [登录日志清理] 开始执行，留存天数: {}", retentionDays);
        int cleanedCount = loginLogService.cleanExpiredLogs(retentionDays);
        log.info("定时任务 [登录日志清理] 执行完成，清理数量: {}", cleanedCount);
    }
}
