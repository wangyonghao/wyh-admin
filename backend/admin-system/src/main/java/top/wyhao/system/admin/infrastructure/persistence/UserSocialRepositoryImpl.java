package top.wyhao.system.admin.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.UserSocialRepository;
import top.wyhao.system.admin.domain.model.UserSocial;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserSocialDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysUserSocialMapper;

import java.util.Collection;
import java.util.List;

/**
 * 用户第三方账号仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class UserSocialRepositoryImpl implements UserSocialRepository {

    private final SysUserSocialMapper socialMapper;

    @Override
    public UserSocial findBySourceAndOpenId(String source, String openId) {
        return AdminDoMapper.toDomain(socialMapper.selectBySourceAndOpenId(source, openId), UserSocial.class);
    }

    @Override
    public void insert(UserSocial social) {
        SysUserSocialDO data = AdminDoMapper.toDo(social, SysUserSocialDO.class);
        socialMapper.insert(data);
        AdminDoMapper.copyBack(data, social);
    }

    @Override
    public void updateLoginMeta(UserSocial social) {
        socialMapper.lambdaUpdate()
                .set(SysUserSocialDO::getMetaJson, social.getMetaJson())
                .set(SysUserSocialDO::getLastLoginTime, social.getLastLoginTime())
                .eq(SysUserSocialDO::getSource, social.getSource())
                .eq(SysUserSocialDO::getOpenId, social.getOpenId())
                .update();
    }

    @Override
    public List<UserSocial> listByUserId(Long userId) {
        return AdminDoMapper.toDomainList(
                socialMapper.lambdaQuery().eq(SysUserSocialDO::getUserId, userId).list(),
                UserSocial.class);
    }

    @Override
    public void deleteBySourceAndUserId(String source, Long userId) {
        socialMapper.lambdaUpdate()
                .eq(SysUserSocialDO::getSource, source)
                .eq(SysUserSocialDO::getUserId, userId)
                .remove();
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        socialMapper.lambdaUpdate().in(SysUserSocialDO::getUserId, userIds).remove();
    }

    @Override
    public void deleteAll() {
        socialMapper.delete(Wrappers.<SysUserSocialDO>query().eq("1", 1));
    }

    @Override
    public void deleteExcludingUserIds(Collection<Long> keepUserIds) {
        socialMapper.lambdaUpdate().notIn(SysUserSocialDO::getUserId, keepUserIds).remove();
    }
}
