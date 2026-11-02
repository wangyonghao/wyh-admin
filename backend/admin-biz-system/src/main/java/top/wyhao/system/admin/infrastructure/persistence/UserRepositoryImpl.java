package top.wyhao.system.admin.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysUserMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper userMapper;

    @Override
    public User findById(Long id) {
        return AdminDoMapper.toDomain(userMapper.selectById(id), User.class);
    }

    @Override
    public User findByUsername(String username) {
        return AdminDoMapper.toDomain(userMapper.selectByUsername(username), User.class);
    }

    @Override
    public User findByPhone(String phone) {
        return AdminDoMapper.toDomain(userMapper.selectByPhone(phone), User.class);
    }

    @Override
    public User findByEmail(String email) {
        return AdminDoMapper.toDomain(userMapper.selectByEmail(email), User.class);
    }

    @Override
    public void insert(User user) {
        SysUserDO data = AdminDoMapper.toDo(user, SysUserDO.class);
        userMapper.insert(data);
        AdminDoMapper.copyBack(data, user);
    }

    @Override
    public void insertBatch(List<User> users) {
        List<SysUserDO> dataList = AdminDoMapper.toDoList(users, SysUserDO.class);
        userMapper.insert(dataList);
        for (int i = 0; i < users.size() && i < dataList.size(); i++) {
            AdminDoMapper.copyBack(dataList.get(i), users.get(i));
        }
    }

    @Override
    public void updateById(User user) {
        SysUserDO data = AdminDoMapper.toDo(user, SysUserDO.class);
        userMapper.updateById(data);
        AdminDoMapper.copyBack(data, user);
    }

    @Override
    public void updateBatch(List<User> users) {
        List<SysUserDO> dataList = AdminDoMapper.toDoList(users, SysUserDO.class);
        userMapper.updateBatchById(dataList);
    }

    @Override
    public void updatePartial(User user) {
        userMapper.lambdaUpdate().update(AdminDoMapper.toDo(user, SysUserDO.class));
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        userMapper.deleteByIds(ids);
    }

    @Override
    public List<User> listBriefByIds(List<Long> ids) {
        return AdminDoMapper.toDomainList(userMapper.lambdaQuery()
                .select(SysUserDO::getId, SysUserDO::getDisplayName, SysUserDO::getIsBuiltin)
                .in(SysUserDO::getId, ids)
                .list(), User.class);
    }

    @Override
    public void updatePasswordAndExpire(Long id, String encodedPassword, LocalDateTime pwdExpireDate) {
        userMapper.lambdaUpdate()
                .set(SysUserDO::getPassword, encodedPassword)
                .set(SysUserDO::getPwdExpireDate, pwdExpireDate)
                .eq(SysUserDO::getId, id)
                .update();
    }

    @Override
    public void updateAvatar(Long id, Long avatarId) {
        userMapper.lambdaUpdate()
                .set(SysUserDO::getAvatar, avatarId)
                .eq(SysUserDO::getId, id)
                .update();
    }

    @Override
    public void updateBasicInfo(Long id, String displayName, Object gender) {
        userMapper.lambdaUpdate()
                .set(SysUserDO::getDisplayName, displayName)
                .set(SysUserDO::getGender, gender)
                .eq(SysUserDO::getId, id)
                .update();
    }

    @Override
    public void updatePassword(Long id, String encodedPassword, LocalDateTime pwdUpdateTime,
                               String passwordHistory) {
        userMapper.lambdaUpdate()
                .set(SysUserDO::getPassword, encodedPassword)
                .set(SysUserDO::getPwdUpdateTime, pwdUpdateTime)
                .set(SysUserDO::getPasswordHistory, passwordHistory)
                .eq(SysUserDO::getId, id)
                .update();
    }

    @Override
    public long countByDepartmentIds(Collection<Long> departmentIds) {
        return userMapper.lambdaQuery().in(SysUserDO::getDepartmentId, departmentIds).count();
    }

    @Override
    public boolean existsUsername(String username) {
        return userMapper.lambdaQuery().eq(SysUserDO::getUsername, username).exists();
    }

    @Override
    public boolean existsEmail(String email, Long excludeUserId) {
        return userMapper.lambdaQuery()
                .eq(SysUserDO::getEmail, email)
                .ne(excludeUserId != null, SysUserDO::getId, excludeUserId)
                .exists();
    }

    @Override
    public boolean existsPhone(String phone, Long excludeUserId) {
        return userMapper.lambdaQuery()
                .eq(SysUserDO::getPhone, phone)
                .ne(excludeUserId != null, SysUserDO::getId, excludeUserId)
                .exists();
    }

    @Override
    public int countByUsernames(Collection<String> usernames) {
        return Math.toIntExact(userMapper.lambdaQuery().in(SysUserDO::getUsername, usernames).count());
    }

    @Override
    public int countByEmails(Collection<String> emails) {
        return Math.toIntExact(userMapper.lambdaQuery().in(SysUserDO::getEmail, emails).count());
    }

    @Override
    public int countByPhones(Collection<String> phones) {
        return Math.toIntExact(userMapper.lambdaQuery().in(SysUserDO::getPhone, phones).count());
    }

    @Override
    public List<String> listEmailsIn(Collection<String> emails) {
        List<SysUserDO> users = userMapper.lambdaQuery()
                .select(SysUserDO::getEmail)
                .in(SysUserDO::getEmail, emails)
                .list();
        return CollUtils.mapToList(users, SysUserDO::getEmail);
    }

    @Override
    public List<String> listPhonesIn(Collection<String> phones) {
        List<SysUserDO> users = userMapper.lambdaQuery()
                .select(SysUserDO::getPhone)
                .in(SysUserDO::getPhone, phones)
                .list();
        return CollUtils.mapToList(users, SysUserDO::getPhone);
    }

    @Override
    public List<User> listByUsernames(Collection<String> usernames) {
        return AdminDoMapper.toDomainList(userMapper.lambdaQuery()
                .in(SysUserDO::getUsername, usernames)
                .select(SysUserDO::getId, SysUserDO::getUsername)
                .list(), User.class);
    }

    @Override
    public List<User> listAll() {
        return AdminDoMapper.toDomainList(userMapper.selectList(null), User.class);
    }

    @Override
    public void deleteAll() {
        userMapper.delete(Wrappers.<SysUserDO>query().eq("1", 1));
    }

    @Override
    public long countExcluding(Collection<Long> keepIds) {
        return userMapper.lambdaQuery().notIn(SysUserDO::getId, keepIds).count();
    }

    @Override
    public boolean deleteExcluding(Collection<Long> keepIds) {
        return userMapper.lambdaUpdate().notIn(SysUserDO::getId, keepIds).remove();
    }
}
