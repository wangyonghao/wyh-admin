package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户仓储。
 */
public interface UserRepository {

    User findById(Long id);

    User findByUsername(String username);

    User findByPhone(String phone);

    User findByEmail(String email);

    void insert(User user);

    void insertBatch(List<User> users);

    void updateById(User user);

    void updateBatch(List<User> users);

    /**
     * 按实体非空字段更新，条件取主键。
     */
    void updatePartial(User user);

    void deleteByIds(List<Long> ids);

    List<User> listBriefByIds(List<Long> ids);

    void updatePasswordAndExpire(Long id, String encodedPassword, LocalDateTime pwdExpireDate);

    void updateAvatar(Long id, Long avatarId);

    void updateBasicInfo(Long id, String displayName, Object gender);

    void updatePassword(Long id, String encodedPassword, LocalDateTime pwdUpdateTime, String passwordHistory);

    long countByDepartmentIds(Collection<Long> departmentIds);

    boolean existsUsername(String username);

    boolean existsEmail(String email, Long excludeUserId);

    boolean existsPhone(String phone, Long excludeUserId);

    int countByUsernames(Collection<String> usernames);

    int countByEmails(Collection<String> emails);

    int countByPhones(Collection<String> phones);

    List<String> listEmailsIn(Collection<String> emails);

    List<String> listPhonesIn(Collection<String> phones);

    List<User> listByUsernames(Collection<String> usernames);

    List<User> listAll();

    void deleteAll();

    long countExcluding(Collection<Long> keepIds);

    boolean deleteExcluding(Collection<Long> keepIds);
}
