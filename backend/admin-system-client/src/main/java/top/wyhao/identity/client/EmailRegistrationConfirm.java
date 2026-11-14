package top.wyhao.identity.client;

/**
 * 确认链接的结果。两种状态都不签发登录 token。
 */
public record EmailRegistrationConfirm(String status) {
    public static final String VERIFIED = "VERIFIED";
    public static final String ALREADY_VERIFIED = "ALREADY_VERIFIED";
}
