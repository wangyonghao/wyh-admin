package top.wyhao.admin.system.otp.service.impl;

import cn.hutool.core.util.ReUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.otp.config.OtpProperties;
import top.wyhao.admin.system.otp.enums.OtpChannel;
import top.wyhao.admin.system.otp.exception.OtpException;
import top.wyhao.admin.system.otp.service.ChannelService;

/**
 * 邮件渠道服务实现
 *
 * @author wyhao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChannelService implements ChannelService {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final JavaMailSender mailSender;
    private final OtpProperties otpProperties;

    @Override
    public OtpChannel getChannel() {
        return OtpChannel.EMAIL;
    }

    @Override
    public void send(String target, String subject, String content) {
        if (!otpProperties.getChannel().getEmail().getEnabled()) {
            throw OtpException.sendFailed("邮件渠道未启用");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(otpProperties.getChannel().getEmail().getFrom());
            message.setTo(target);
            message.setSubject(otpProperties.getChannel().getEmail().getSubjectPrefix() + subject);
            message.setText(content);

            mailSender.send(message);
            log.info("邮件发送成功: target={}", target);
        } catch (Exception e) {
            log.error("邮件发送失败: target={}, error={}", target, e.getMessage(), e);
            throw OtpException.sendFailed("邮件发送失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateTarget(String target) {
        return ReUtil.isMatch(EMAIL_PATTERN, target);
    }
}
