
package top.wyhao.admin.job;

import cn.hutool.core.text.CharSequenceUtil;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.open.mapper.SysAppMapper;
import top.wyhao.admin.open.model.entity.SysApp;
import top.wyhao.admin.system.entity.*;
import top.wyhao.admin.system.entity.SysUser;
import top.wyhao.admin.system.entity.SysUserSocial;
import top.wyhao.admin.system.mapper.*;
import top.wyhao.admin.system.mapper.SysUserMapper;
import top.wyhao.admin.system.mapper.SysUserSocialMapper;
import top.wyhao.admin.tenant.mapper.SysTenantMapper;
import top.wyhao.admin.tenant.mapper.TenantPackageMapper;
import top.wyhao.admin.tenant.mapper.TenantPackageMenuMapper;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.constant.StringConstants;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * 演示环境任务（任务示例）
 *

 * @since 2024/8/4 15:30
 */
@Component
@RequiredArgsConstructor
public class DemoEnvironmentJob {

    private final SysDictMapper dictMapper;
    private final SysNoticeMapper noticeMapper;
    private final SysNoticeLogMapper noticeLogMapper;
    private final SysMessageMapper messageMapper;
    private final SysMessageLogMapper messageLogMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserSocialMapper userSocialMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SysDeptMapper deptMapper;
    private final SysAppMapper appMapper;
    private final SysTenantMapper tenantMapper;
    private final TenantPackageMapper packageMapper;
    private final TenantPackageMenuMapper packageMenuMapper;

    private static final Long DELETE_FLAG = 10000L;
    private static final Long MESSAGE_FLAG = 0L;
    private static final List<Long> USER_FLAG = List
            .of(1L, 801822L, 801823L, 801824L, 801825L, 801826L, 801827L, 801828L, 801829L, 801830L, 801831L, 801832L, 801833L, 801834L);
    private static final List<Long> ROLE_FLAG = List.of(1L, 2L, 3L, 547888897925840927L, 547888897925840928L);
    private static final Long DEPT_FLAG = 547887852587843611L;

    /**
     * 重置演示环境数据
     */
    @JobExecutor(name = "ResetEnvironmentData")
    @Transactional(rollbackFor = Exception.class)
    public void resetEnvironmentData() {
        try {
            SnailJobLog.REMOTE.info("定时任务 [重置演示环境数据] 开始执行。");
            // 检测待清理数据
            SnailJobLog.REMOTE.info("开始检测演示环境待清理数据项，请稍候...");
            Long dictCount = dictMapper.lambdaQuery().gt(SysDict::getId, DELETE_FLAG).count();
            this.log(dictCount, "字典");
            Long noticeCount = noticeMapper.lambdaQuery().gt(SysNotice::getId, DELETE_FLAG).count();
            this.log(noticeCount, "公告");
            Long messageCount = messageMapper.lambdaQuery().count();
            this.log(messageCount, "通知");
            Long userCount = userMapper.lambdaQuery().notIn(SysUser::getId, USER_FLAG).count();
            this.log(userCount, "用户");
            Long roleCount = roleMapper.lambdaQuery().notIn(SysRole::getId, ROLE_FLAG).count();
            this.log(roleCount, "角色");
            Long menuCount = menuMapper.lambdaQuery().gt(SysMenu::getId, DELETE_FLAG).count();
            this.log(menuCount, "菜单");
            Long deptCount = deptMapper.lambdaQuery().gt(SysDept::getId, DEPT_FLAG).count();
            this.log(deptCount, "部门");
            Long appCount = appMapper.lambdaQuery().gt(SysApp::getId, DELETE_FLAG).count();
            this.log(appCount, "应用");
            Long tenantCount = tenantMapper.lambdaQuery().count();
            this.log(tenantCount, "租户");
            Long packageCount = packageMapper.lambdaQuery().count();
            this.log(packageCount, "套餐");
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().blockAttack(true).build());
            SnailJobLog.REMOTE.info("演示环境待清理数据项检测完成，开始执行清理。");
            // 清理关联数据
            noticeLogMapper.lambdaUpdate().gt(SysNoticeLog::getNoticeId, DELETE_FLAG).remove();
            messageLogMapper.lambdaUpdate().gt(SysMessageLog::getMessageId, MESSAGE_FLAG).remove();
            userRoleMapper.lambdaUpdate().notIn(SysUserRole::getRoleId, ROLE_FLAG).remove();
            userRoleMapper.lambdaUpdate().notIn(SysUserRole::getUserId, USER_FLAG).remove();
            roleDeptMapper.lambdaUpdate().notIn(SysRoleDept::getRoleId, ROLE_FLAG).remove();
            roleMenuMapper.lambdaUpdate().notIn(SysRoleMenu::getRoleId, ROLE_FLAG).remove();
            userSocialMapper.lambdaUpdate().notIn(SysUserSocial::getUserId, USER_FLAG).remove();
            packageMenuMapper.lambdaUpdate().remove();
            // 清理具体数据
            this.clean(dictCount, "字典", CacheConstants.DICT_KEY_PREFIX, () -> dictMapper.lambdaUpdate()
                .gt(SysDict::getId, DELETE_FLAG)
                .remove());
            this.clean(noticeCount, "公告", null, () -> noticeMapper.lambdaUpdate()
                .gt(SysNotice::getId, DELETE_FLAG)
                .remove());
            this.clean(messageCount, "通知", null, () -> messageMapper.lambdaUpdate()
                .gt(SysMessage::getId, MESSAGE_FLAG)
                .remove());
            this.clean(userCount, "用户", null, () -> userMapper.lambdaUpdate().notIn(SysUser::getId, USER_FLAG).remove());
            this.clean(roleCount, "角色", null, () -> roleMapper.lambdaUpdate().notIn(SysRole::getId, ROLE_FLAG).remove());
            this.clean(menuCount, "菜单", CacheConstants.ROLE_MENU_KEY_PREFIX, () -> menuMapper.lambdaUpdate()
                .gt(SysMenu::getId, DELETE_FLAG)
                .remove());
            this.clean(deptCount, "部门", null, () -> deptMapper.lambdaUpdate().gt(SysDept::getId, DEPT_FLAG).remove());
            this.clean(appCount, "应用", null, () -> appMapper.lambdaUpdate().gt(SysApp::getId, DEPT_FLAG).remove());
            this.clean(tenantCount, "租户", null, () -> tenantMapper.lambdaUpdate().remove());
            this.clean(packageCount, "套餐", null, () -> packageMapper.lambdaUpdate().remove());
            SnailJobLog.REMOTE.info("演示环境数据已清理完成。");
            SnailJobLog.REMOTE.info("定时任务 [重置演示环境数据] 执行结束。");
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
            SnailJobLog.REMOTE.info("检测到 [{}] 待清理数据项：{}条", resource, count);
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
            SnailJobLog.REMOTE.info("[{}] 数据项清理完成。", resource);
            if (CharSequenceUtil.isNotBlank(cacheKey)) {
                RedisUtils.deleteByPattern(cacheKey + StringConstants.ASTERISK);
                SnailJobLog.REMOTE.info("[{}] 数据项缓存清理完成。", resource);
            }
        }
    }
}
