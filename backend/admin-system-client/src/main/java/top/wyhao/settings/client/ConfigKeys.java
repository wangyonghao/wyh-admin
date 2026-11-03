package top.wyhao.settings.client;

/**
 * 配置键约定。业务模块新增配置项时在此（或本域常量）声明 key，经 {@link ConfigStoreApi} 读写，无需改 settings 实现。
 */
public final class ConfigKeys {

    public static final String SITE = "site";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String MAIL = "mail";
    public static final String SMS = "sms";
    public static final String STORAGE = "storage";
    /** 密码策略配置 */
    public static final String PASSWORD_POLICY = "password-policy";

    public static String smsTemplate(String scene) {
        return "sms-template-" + scene;
    }

    private ConfigKeys() {
    }
}
