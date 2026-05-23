package top.wyhao.admin.job;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.wyhao.admin.system.service.LoginLogService;

/**
 * 登录日志清理定时任务
 *

 * @since 2026/05/08
 */
@Component
@RequiredArgsConstructor
public class LoginLogCleanupJob {

    private final LoginLogService loginLogService;

    /**
     * 登录日志留存天数，默认365天（1年）
     */
    @Value("${system.login-log.retention-days:365}")
    private int retentionDays;

    /**
     * 是否启用自动清理，默认启用
     */
    @Value("${system.login-log.auto-cleanup-enabled:true}")
    private boolean autoCleanupEnabled;

    /**
     * 清理过期登录日志
     */
    @JobExecutor(name = "CleanExpiredLoginLogs")
    public void cleanExpiredLogs() {
        if (!autoCleanupEnabled) {
            SnailJobLog.REMOTE.info("定时任务 [清理过期登录日志] 已禁用，跳过执行。");
            return;
        }

        SnailJobLog.REMOTE.info("定时任务 [清理过期登录日志] 开始执行，留存天数: {}", retentionDays);
        
        try {
            int cleanedCount = loginLogService.cleanExpiredLogs(retentionDays);
            SnailJobLog.REMOTE.info("定时任务 [清理过期登录日志] 执行完成，清理数量: {}", cleanedCount);
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("定时任务 [清理过期登录日志] 执行失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}
