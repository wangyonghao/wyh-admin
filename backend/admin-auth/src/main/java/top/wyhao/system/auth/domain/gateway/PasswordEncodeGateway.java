package top.wyhao.system.auth.domain.gateway;

/**
 * 密码不得明文存储或使用可逆加密
 */
public interface PasswordEncodeGateway {
    /**
     * 慢哈希密码
     * @param input 密码原文
     * @return 密码哈希值
     */
    String encode(String input);

    /**
     * 验证用户输入密码与哈希值是否匹配
     * @param input 用户输入密码
     * @param encoded 密码哈希
     * @return 匹配返回 true, 否则返回 false
     */
    boolean matches(String input, String encoded);
}
