
package top.wyhao.admin.system.service;

import me.zhyd.oauth.model.AuthUser;
import top.wyhao.admin.system.entity.SysUserSocial;

import java.util.List;

/**
 * 用户社会化关联业务接口
 *

 * @since 2023/10/11 22:10
 */
public interface UserSocialService {

    /**
     * 根据来源和开放 ID 查询
     *
     * @param source 来源
     * @param openId 开放 ID
     * @return 用户社会化关联信息
     */
    SysUserSocial getBySourceAndOpenId(String source, String openId);

    /**
     * 保存
     *
     * @param userSocial 用户社会化关联信息
     */
    void saveOrUpdate(SysUserSocial userSocial);

    /**
     * 根据用户 ID 查询
     *
     * @param userId 用户 ID
     * @return 用户社会化关联信息
     */
    List<SysUserSocial> listByUserId(Long userId);

    /**
     * 绑定
     *
     * @param authUser 三方账号信息
     * @param userId   用户 ID
     */
    void bind(AuthUser authUser, Long userId);

    /**
     * 根据来源和用户 ID 删除
     *
     * @param source 来源
     * @param userId 用户 ID
     */
    void deleteBySourceAndUserId(String source, Long userId);

    /**
     * 根据用户 ID 删除
     *
     * @param userIds 用户 ID 列表
     */
    void deleteByUserIds(List<Long> userIds);
}