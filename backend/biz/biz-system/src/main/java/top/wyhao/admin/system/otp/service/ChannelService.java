package top.wyhao.admin.system.otp.service;

import top.wyhao.admin.system.otp.enums.OtpChannel;

/**
 * 渠道服务接口
 *
 * @author wyhao
 */
public interface ChannelService {

    /**
     * 获取支持的渠道
     *
     * @return 渠道类型
     */
    OtpChannel getChannel();

    /**
     * 发送验证码
     *
     * @param target  目标地址
     * @param subject 主题（邮件使用）
     * @param content 内容
     */
    void send(String target, String subject, String content);

    /**
     * 验证目标地址格式
     *
     * @param target 目标地址
     * @return 是否有效
     */
    boolean validateTarget(String target);
}
