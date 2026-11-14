package top.wyhao.identity.client;

/**
 * 待验证结果。不含登录 token，也不含邮件里的验证原文。
 *
 * @param status              PENDING 已接受发送；COOLDOWN 仍在重发间隔内，没有新发信
 * @param delivered           本次调用是否已得到邮件服务器接受。入队或未发送都是 false
 * @param resendAfterSeconds  距离允许下一次发送的秒数
 * @param expiresMinutes      当前链接有效分钟数
 */
public record EmailRegistrationTicket(
        String status,
        boolean delivered,
        int resendAfterSeconds,
        int expiresMinutes
) {
    public static final String PENDING = "PENDING";
    public static final String COOLDOWN = "COOLDOWN";
}
