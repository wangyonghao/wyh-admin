package top.wyhao.admin.system.otp.service.impl;

import cn.hutool.core.util.ReUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.otp.config.OtpProperties;
import top.wyhao.admin.system.otp.enums.OtpChannel;
import top.wyhao.admin.system.otp.exception.OtpException;
import top.wyhao.admin.system.otp.service.ChannelService;

/**
 * 短信渠道服务实现
 *

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsChannelService implements ChannelService {

    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    private final OtpProperties otpProperties;

    @Override
    public OtpChannel getChannel() {
        return OtpChannel.SMS;
    }

    @Override
    public void send(String target, String subject, String content) {
        if (!otpProperties.getChannel().getSms().getEnabled()) {
            throw OtpException.sendFailed("短信渠道未启用");
        }

        try {
            // TODO: 集成实际的短信服务商 API（阿里云、腾讯云等）
            log.info("短信发送成功: target={}, content={}", target, content);
            
            // 示例：调用短信服务商 API
            // SmsClient client = new SmsClient();
            // client.send(target, otpProperties.getChannel().getSms().getSignName(), content);
            
        } catch (Exception e) {
            log.error("短信发送失败: target={}, error={}", target, e.getMessage(), e);
            throw OtpException.sendFailed("短信发送失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateTarget(String target) {
        return ReUtil.isMatch(PHONE_PATTERN, target);
    }
}
