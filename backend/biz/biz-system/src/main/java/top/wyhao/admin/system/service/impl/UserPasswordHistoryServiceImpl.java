
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.entity.SysUserPasswordHistory;
import top.wyhao.admin.system.mapper.SysUserPasswordHistoryMapper;
import top.wyhao.admin.system.service.UserPasswordHistoryService;
import top.wyhao.starter.core.util.CollUtils;

import java.util.List;

/**
 * 用户历史密码业务实现
 *

 * @since 2024/5/16 21:58
 */
@Service
@RequiredArgsConstructor
public class UserPasswordHistoryServiceImpl implements UserPasswordHistoryService {

    private final SysUserPasswordHistoryMapper baseMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, String password, int count) {
        if (StrUtil.isBlank(password)) {
            return;
        }
        baseMapper.insert(new SysUserPasswordHistory(userId, password));
        // 删除过期历史密码
        baseMapper.deleteExpired(userId, count);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(SysUserPasswordHistory::getUserId, userIds).remove();
    }

    @Override
    public boolean isPasswordReused(Long userId, String password, int count) {
        // 查询近 N 个历史密码
        List<SysUserPasswordHistory> list = baseMapper.lambdaQuery()
            .select(SysUserPasswordHistory::getPassword)
            .eq(SysUserPasswordHistory::getUserId, userId)
            .orderByDesc(SysUserPasswordHistory::getCreateTime)
            .last("LIMIT %s".formatted(count))
            .list();
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        // 校验是否重复使用历史密码
        List<String> passwordList = CollUtils.mapToList(list, SysUserPasswordHistory::getPassword);
        return passwordList.stream().anyMatch(p -> passwordEncoder.matches(password, p));
    }
}