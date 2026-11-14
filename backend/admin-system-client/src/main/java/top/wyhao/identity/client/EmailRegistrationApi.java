package top.wyhao.identity.client;

/**
 * 邮箱注册验证。确认前不创建可登录账号，也不签发 token。
 */
public interface EmailRegistrationApi {

    EmailRegistrationTicket begin(EmailRegistrationStart start);

    /**
     * 对已有待验证记录重发。没有待验证记录时不发信，也不说明该邮箱是否已注册。
     */
    EmailRegistrationTicket resend(String email, String publicBaseUrl, int ttlMinutes, int resendIntervalSeconds);

    /**
     * 消费一次性 token 并创建账号。已确认过的链接返回友好状态，不再建号。
     *
     * @param registrationEnabled 公开注册开关。关闭时不完成建号。
     */
    EmailRegistrationConfirm confirm(String token, boolean registrationEnabled);
}
