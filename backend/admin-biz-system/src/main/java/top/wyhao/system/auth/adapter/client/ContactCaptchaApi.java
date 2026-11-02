package top.wyhao.system.auth.adapter.client;

/**
 * 修改手机、邮箱时校验联系方式验证码。
 */
public interface ContactCaptchaApi {

    void verifyPhone(String phone, String captcha);

    void verifyEmail(String email, String captcha);
}
