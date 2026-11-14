package top.wyhao.system.admin.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.EmailRegistrationRepository;
import top.wyhao.system.admin.domain.model.EmailRegistration;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysEmailRegistrationDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysEmailRegistrationMapper;

import java.time.LocalDateTime;

/**
 * 待验证注册仓储。token 行锁和后续更新必须在同一事务里。
 */
@Repository
@RequiredArgsConstructor
public class EmailRegistrationRepositoryImpl implements EmailRegistrationRepository {

    private final SysEmailRegistrationMapper mapper;

    @Override
    public void deleteStale(LocalDateTime now) {
        mapper.delete(Wrappers.lambdaQuery(SysEmailRegistrationDO.class)
                .isNull(SysEmailRegistrationDO::getConsumedAt)
                .lt(SysEmailRegistrationDO::getExpiresAt, now));
        mapper.delete(Wrappers.lambdaQuery(SysEmailRegistrationDO.class)
                .lt(SysEmailRegistrationDO::getExpiresAt, now.minusDays(1)));
    }

    @Override
    public EmailRegistration findOpen(Long tenantId, String email) {
        Long id = mapper.findOpenId(tenantId, email);
        if (id == null) {
            return null;
        }
        return toDomain(mapper.selectById(id));
    }

    @Override
    public EmailRegistration lockByTokenHash(String tokenHash) {
        Long id = mapper.lockIdByTokenHash(tokenHash);
        if (id == null) {
            return null;
        }
        return toDomain(mapper.selectById(id));
    }

    @Override
    public void insert(EmailRegistration registration) {
        SysEmailRegistrationDO data = toDo(registration);
        mapper.insert(data);
        registration.setId(data.getId());
    }

    @Override
    public void update(EmailRegistration registration) {
        mapper.updateById(toDo(registration));
    }

    @Override
    public void markSend(Long id, String sendStatus, LocalDateTime sentAt, LocalDateTime now) {
        SysEmailRegistrationDO current = mapper.selectById(id);
        if (current == null) {
            return;
        }
        current.setSendStatus(sendStatus);
        current.setSentAt(sentAt);
        current.setUpdateTime(now);
        mapper.updateById(current);
    }

    private static EmailRegistration toDomain(SysEmailRegistrationDO data) {
        if (data == null) {
            return null;
        }
        EmailRegistration registration = new EmailRegistration();
        registration.setId(data.getId());
        registration.setTenantId(data.getTenantId());
        registration.setEmail(data.getEmail());
        registration.setUsername(data.getUsername());
        registration.setPasswordHash(data.getPasswordHash());
        registration.setTokenHash(data.getTokenHash());
        registration.setDepartmentId(data.getDepartmentId());
        registration.setRoleId(data.getRoleId());
        registration.setExpiresAt(data.getExpiresAt());
        registration.setSentAt(data.getSentAt());
        registration.setSendStatus(data.getSendStatus());
        registration.setConsumedAt(data.getConsumedAt());
        registration.setCreateTime(data.getCreateTime());
        registration.setUpdateTime(data.getUpdateTime());
        return registration;
    }

    private static SysEmailRegistrationDO toDo(EmailRegistration registration) {
        SysEmailRegistrationDO data = new SysEmailRegistrationDO();
        data.setId(registration.getId());
        data.setTenantId(registration.getTenantId());
        data.setEmail(registration.getEmail());
        data.setUsername(registration.getUsername());
        data.setPasswordHash(registration.getPasswordHash());
        data.setTokenHash(registration.getTokenHash());
        data.setDepartmentId(registration.getDepartmentId());
        data.setRoleId(registration.getRoleId());
        data.setExpiresAt(registration.getExpiresAt());
        data.setSentAt(registration.getSentAt());
        data.setSendStatus(registration.getSendStatus());
        data.setConsumedAt(registration.getConsumedAt());
        data.setCreateTime(registration.getCreateTime());
        data.setUpdateTime(registration.getUpdateTime());
        return data;
    }
}
