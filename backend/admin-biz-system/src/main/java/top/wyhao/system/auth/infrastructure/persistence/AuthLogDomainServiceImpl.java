package top.wyhao.system.auth.infrastructure.persistence;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.auth.domain.authlog.AuthLog;
import top.wyhao.system.auth.domain.authlog.AuthLogCriteria;
import top.wyhao.system.auth.domain.authlog.AuthLogDomainService;
import top.wyhao.system.auth.infrastructure.persistence.mapper.SysAuthenticationLogMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志仓储实现：domain ↔ 持久化实体映射。
 */
@Repository
@RequiredArgsConstructor
public class AuthLogDomainServiceImpl implements AuthLogDomainService {

    private final SysAuthenticationLogMapper authenticationLogMapper;

    @Override
    public void insert(AuthLog loginLog) {
        authenticationLogMapper.insert(toEntity(loginLog));
    }

    @Override
    public PageResult<AuthLog> page(AuthLogCriteria criteria, long page, long pageSize) {
        IPage<SysAuthenticationLog> result = authenticationLogMapper.selectPage(new Page<>(page, pageSize), wrapper(criteria));
        return PageResult.of(result).map(this::toDomain);
    }

    @Override
    public List<AuthLog> list(AuthLogCriteria criteria) {
        return authenticationLogMapper.selectList(wrapper(criteria)).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteBefore(LocalDateTime expireTime) {
        return authenticationLogMapper.delete(new LambdaQueryWrapper<SysAuthenticationLog>()
                .lt(SysAuthenticationLog::getLoginTime, expireTime));
    }

    private LambdaQueryWrapper<SysAuthenticationLog> wrapper(AuthLogCriteria criteria) {
        LambdaQueryWrapper<SysAuthenticationLog> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(criteria.getUsername())) {
            queryWrapper.like(SysAuthenticationLog::getUsername, criteria.getUsername());
        }
        if (CharSequenceUtil.isNotBlank(criteria.getIpAddress())) {
            queryWrapper.like(SysAuthenticationLog::getIpAddress, criteria.getIpAddress());
        }
        if (criteria.getLoginStatus() != null) {
            queryWrapper.eq(SysAuthenticationLog::getLoginStatus, criteria.getLoginStatus());
        }
        if (criteria.getLoginTimeStart() != null) {
            queryWrapper.ge(SysAuthenticationLog::getLoginTime, criteria.getLoginTimeStart());
        }
        if (criteria.getLoginTimeEnd() != null) {
            queryWrapper.le(SysAuthenticationLog::getLoginTime, criteria.getLoginTimeEnd());
        }
        if (criteria.isTenantRestricted()) {
            queryWrapper.eq(SysAuthenticationLog::getTenantId, criteria.getTenantId());
        }
        queryWrapper.orderByDesc(SysAuthenticationLog::getLoginTime);
        return queryWrapper;
    }

    private SysAuthenticationLog toEntity(AuthLog log) {
        SysAuthenticationLog entity = new SysAuthenticationLog();
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

    private AuthLog toDomain(SysAuthenticationLog entity) {
        AuthLog log = new AuthLog();
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
