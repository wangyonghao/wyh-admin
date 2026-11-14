package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.EmailRegistration;

import java.time.LocalDateTime;

/**
 * 待验证邮箱注册。按邮箱查找走加密列，按 token 摘要加行锁。
 */
public interface EmailRegistrationRepository {

    /**
     * 过期且未确认的记录立即删掉，邮箱不再被占用。
     * 已确认记录再保留一天，供过期后的重放给出友好状态。
     */
    void deleteStale(LocalDateTime now);

    EmailRegistration findOpen(Long tenantId, String email);

    EmailRegistration lockByTokenHash(String tokenHash);

    void insert(EmailRegistration registration);

    void update(EmailRegistration registration);

    void markSend(Long id, String sendStatus, LocalDateTime sentAt, LocalDateTime now);
}
