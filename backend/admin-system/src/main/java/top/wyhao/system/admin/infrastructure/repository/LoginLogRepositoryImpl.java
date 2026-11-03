package top.wyhao.system.admin.infrastructure.repository;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.gateway.LoginLogRepository;
import top.wyhao.system.admin.domain.model.LoginLog;
import top.wyhao.system.admin.domain.model.LoginLogCriteria;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysLoginLogDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysLoginLogMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志仓储实现：domain ↔ 持久化实体映射。
 */
@Repository
@RequiredArgsConstructor
public class LoginLogRepositoryImpl implements LoginLogRepository {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public void insert(LoginLog loginLog) {
        loginLogMapper.insert(toEntity(loginLog));
    }

    @Override
    public PageResult<LoginLog> page(LoginLogCriteria criteria, long page, long pageSize) {
        IPage<SysLoginLogDO> result = loginLogMapper.selectPage(new Page<>(page, pageSize), wrapper(criteria));
        return PageResult.of(result).map(this::toDomain);
    }

    @Override
    public List<LoginLog> list(LoginLogCriteria criteria) {
        return loginLogMapper.selectList(wrapper(criteria)).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteBefore(LocalDateTime expireTime) {
        return loginLogMapper.delete(new LambdaQueryWrapper<SysLoginLogDO>()
                .lt(SysLoginLogDO::getLoginTime, expireTime));
    }

    private LambdaQueryWrapper<SysLoginLogDO> wrapper(LoginLogCriteria criteria) {
        LambdaQueryWrapper<SysLoginLogDO> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(criteria.getUsername())) {
            queryWrapper.like(SysLoginLogDO::getUsername, criteria.getUsername());
        }
        if (CharSequenceUtil.isNotBlank(criteria.getIpAddress())) {
            queryWrapper.like(SysLoginLogDO::getIpAddress, criteria.getIpAddress());
        }
        if (criteria.getLoginStatus() != null) {
            queryWrapper.eq(SysLoginLogDO::getLoginStatus, criteria.getLoginStatus());
        }
        if (criteria.getLoginTimeStart() != null) {
            queryWrapper.ge(SysLoginLogDO::getLoginTime, criteria.getLoginTimeStart());
        }
        if (criteria.getLoginTimeEnd() != null) {
            queryWrapper.le(SysLoginLogDO::getLoginTime, criteria.getLoginTimeEnd());
        }
        if (criteria.isTenantRestricted()) {
            queryWrapper.eq(SysLoginLogDO::getTenantId, criteria.getTenantId());
        }
        queryWrapper.orderByDesc(SysLoginLogDO::getLoginTime);
        return queryWrapper;
    }

    private SysLoginLogDO toEntity(LoginLog log) {
        SysLoginLogDO entity = new SysLoginLogDO();
        entity.setId(log.getId());
        entity.setUsername(log.getUsername());
        entity.setIpAddress(log.getIpAddress());
        entity.setLocation(log.getLocation());
        entity.setDeviceType(log.getDeviceType());
        entity.setBrowser(log.getBrowser());
        entity.setOs(log.getOs());
        entity.setLoginStatus(log.getLoginStatus());
        entity.setLoginTime(log.getLoginTime());
        entity.setFailureReason(log.getFailureReason());
        entity.setUserAgent(log.getUserAgent());
        entity.setTenantId(log.getTenantId());
        return entity;
    }

    private LoginLog toDomain(SysLoginLogDO entity) {
        LoginLog log = new LoginLog();
        log.setId(entity.getId());
        log.setUsername(entity.getUsername());
        log.setIpAddress(entity.getIpAddress());
        log.setLocation(entity.getLocation());
        log.setDeviceType(entity.getDeviceType());
        log.setBrowser(entity.getBrowser());
        log.setOs(entity.getOs());
        log.setLoginStatus(entity.getLoginStatus());
        log.setLoginTime(entity.getLoginTime());
        log.setFailureReason(entity.getFailureReason());
        log.setUserAgent(entity.getUserAgent());
        log.setTenantId(entity.getTenantId());
        return log;
    }
}
