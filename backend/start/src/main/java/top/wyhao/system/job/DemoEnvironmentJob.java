
package top.wyhao.system.job;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.open.mapper.SysAppMapper;
import top.wyhao.system.open.model.entity.SysApp;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMessageDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMessageLogDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysNoticeDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysNoticeLogDO;
import top.wyhao.system.admin.domain.gateway.OptionRepository;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysMessageLogMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysMessageMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysNoticeLogMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysNoticeMapper;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.gateway.UserSocialRepository;
import top.wyhao.system.admin.domain.gateway.DepartmentRepository;
import top.wyhao.system.admin.domain.gateway.MenuRepository;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.system.admin.domain.gateway.RoleMenuRepository;
import top.wyhao.system.admin.domain.gateway.RoleRepository;
import top.wyhao.system.admin.domain.gateway.UserRoleRepository;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysTenantMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.TenantPackageMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.TenantPackageMenuMapper;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.cmn.core.constant.CacheConstants;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.starter.quartz.annotation.JobHandler;
import top.wyhao.starter.quartz.spi.JobContext;
import top.wyhao.starter.quartz.spi.JobTask;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * 演示环境任务（任务示例）
 *

 * @since 2024/8/4 15:30
 */
@Slf4j
@JobHandler(code = "demoEnvReset", name = "演示环境重置", description = "清理演示数据，仅演示环境使用")
@RequiredArgsConstructor
public class DemoEnvironmentJob implements JobTask {

    private final OptionRepository optionRepository;
    private final SysNoticeMapper noticeMapper;
    private final SysNoticeLogMapper noticeLogMapper;
    private final SysMessageMapper messageMapper;
    private final SysMessageLogMapper messageLogMapper;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSocialRepository userSocialRepository;
    private final RoleRepository roleRepository;
    private final RoleDepartmentRepository roleDepartmentRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;
    private final DepartmentRepository departmentRepository;
    private final SysAppMapper appMapper;
    private final SysTenantMapper tenantMapper;
    private final TenantPackageMapper packageMapper;
    private final TenantPackageMenuMapper packageMenuMapper;

    private static final Long DELETE_FLAG = 10000L;
    private static final Long MESSAGE_FLAG = 0L;
    private static final List<Long> USER_FLAG = List
            .of(1L, 801822L, 801823L, 801824L, 801825L, 801826L, 801827L, 801828L, 801829L, 801830L, 801831L, 801832L, 801833L, 801834L);
    private static final List<Long> ROLE_FLAG = List.of(1L, 2L, 3L, 547888897925840927L, 547888897925840928L);
    private static final Long DEPARTMENT_FLAG = 547887852587843611L;

    /**
     * 重置演示环境数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(JobContext context) {
        try {
            log.info("定时任务 [重置演示环境数据] 开始执行。");
            // 检测待清理数据
            log.info("开始检测演示环境待清理数据项，请稍候...");
            Long dictCount = optionRepository.countIdGreaterThan(DELETE_FLAG);
            this.log(dictCount, "字典");
            Long noticeCount = noticeMapper.lambdaQuery().gt(SysNoticeDO::getId, DELETE_FLAG).count();
            this.log(noticeCount, "公告");
            Long messageCount = messageMapper.lambdaQuery().count();
            this.log(messageCount, "通知");
            Long userCount = userRepository.countExcluding(USER_FLAG);
            this.log(userCount, "用户");
            Long roleCount = roleRepository.countExcluding(ROLE_FLAG);
            this.log(roleCount, "角色");
            Long menuCount = menuRepository.countIdGreaterThan(DELETE_FLAG);
            this.log(menuCount, "菜单");
            Long departmentCount = departmentRepository.countIdGreaterThan(DEPARTMENT_FLAG);
            this.log(departmentCount, "部门");
            Long appCount = appMapper.lambdaQuery().gt(SysApp::getId, DELETE_FLAG).count();
            this.log(appCount, "应用");
            Long tenantCount = tenantMapper.lambdaQuery().count();
            this.log(tenantCount, "租户");
            Long packageCount = packageMapper.lambdaQuery().count();
            this.log(packageCount, "套餐");
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().blockAttack(true).build());
            log.info("演示环境待清理数据项检测完成，开始执行清理。");
            // 清理关联数据
            noticeLogMapper.lambdaUpdate().gt(SysNoticeLogDO::getNoticeId, DELETE_FLAG).remove();
            messageLogMapper.lambdaUpdate().gt(SysMessageLogDO::getMessageId, MESSAGE_FLAG).remove();
            userRoleRepository.deleteByRoleIdNotIn(ROLE_FLAG);
            userRoleRepository.deleteByUserIdNotIn(USER_FLAG);
            roleDepartmentRepository.deleteByRoleIdNotIn(ROLE_FLAG);
            roleMenuRepository.deleteByRoleIdNotIn(ROLE_FLAG);
            userSocialRepository.deleteExcludingUserIds(USER_FLAG);
            packageMenuMapper.lambdaUpdate().remove();
            // 清理具体数据
            this.clean(dictCount, "选项", CacheConstants.OPTION_KEY_PREFIX,
                () -> optionRepository.deleteIdGreaterThan(DELETE_FLAG));
            this.clean(noticeCount, "公告", null, () -> noticeMapper.lambdaUpdate()
                .gt(SysNoticeDO::getId, DELETE_FLAG)
                .remove());
            this.clean(messageCount, "通知", null, () -> messageMapper.lambdaUpdate()
                .gt(SysMessageDO::getId, MESSAGE_FLAG)
                .remove());
            this.clean(userCount, "用户", null, () -> userRepository.deleteExcluding(USER_FLAG));
            this.clean(roleCount, "角色", null, () -> roleRepository.deleteExcluding(ROLE_FLAG));
            this.clean(menuCount, "菜单", CacheConstants.ROLE_MENU_KEY_PREFIX, () -> menuRepository.deleteIdGreaterThan(DELETE_FLAG));
            this.clean(departmentCount, "部门", null, () -> departmentRepository.deleteIdGreaterThan(DEPARTMENT_FLAG));
            this.clean(appCount, "应用", null, () -> appMapper.lambdaUpdate().gt(SysApp::getId, DEPARTMENT_FLAG).remove());
            this.clean(tenantCount, "租户", null, () -> tenantMapper.lambdaUpdate().remove());
            this.clean(packageCount, "套餐", null, () -> packageMapper.lambdaUpdate().remove());
            log.info("演示环境数据已清理完成。");
            log.info("定时任务 [重置演示环境数据] 执行结束。");
        } finally {
            InterceptorIgnoreHelper.clearIgnoreStrategy();
        }
    }

    /**
     * 输出日志
     *
     * @param count    待清理数据项数量
     * @param resource 资源名称
     */
    private void log(Long count, String resource) {
        if (count > 0) {
            log.info("检测到 [{}] 待清理数据项：{}条", resource, count);
        }
    }

    /**
     * 清理数据
     *
     * @param count    待清理数据项数量
     * @param resource 资源名称
     * @param cacheKey 缓存键
     * @param supplier 清理数据项函数
     */
    private void clean(Long count, String resource, String cacheKey, BooleanSupplier supplier) {
        if (count > 0 && supplier.getAsBoolean()) {
            log.info("[{}] 数据项清理完成。", resource);
            if (CharSequenceUtil.isNotBlank(cacheKey)) {
                RedisUtils.deleteByPattern(cacheKey + StringConstants.ASTERISK);
                log.info("[{}] 数据项缓存清理完成。", resource);
            }
        }
    }
}
