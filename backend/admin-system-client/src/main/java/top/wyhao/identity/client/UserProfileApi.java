package top.wyhao.identity.client;

/**
 * 用户档案查询（跨模块 SPI，供 settings 等模块使用）。
 */
public interface UserProfileApi {

    UserProfile profile(Long id);
}
