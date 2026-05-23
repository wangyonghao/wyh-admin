package top.wyhao.admin.cmn.mail;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import top.wyhao.starter.core.model.MailConfig;
import top.wyhao.starter.core.spi.MailConfigProvider;

import java.io.File;
import java.util.*;

/**
 * 邮件服务类
 *

 * @since 2026/4/27
 */
@Service
@RequiredArgsConstructor
public class MailClient {

    private final MailConfigProvider mailConfigProvider;

    private volatile MailConfig mailConfig;
    private volatile JavaMailSender javaMailSender;

    /**
     * 发送文本邮件给单个人
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendText(String to, String subject, String content) {
        send(splitAddress(to), null, null, subject, content, false);
    }

    /**
     * 发送 HTML 邮件给单个人
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendHtml(String to, String subject, String content) {
        send(splitAddress(to), null, null, subject, content, true);
    }

    /**
     * 发送 HTML 邮件给单个人（带附件）
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     * @param files   附件列表
     */
    public void sendHtml(String to, String subject, String content, File... files) {
        send(splitAddress(to), null, null, subject, content, true, files);
    }

    /**
     * 发送 HTML 邮件给多个人
     *
     * @param tos     收件人列表
     * @param subject 主题
     * @param content 内容
     * @param files   附件列表
     */
    public void sendHtml(Collection<String> tos,
                         String subject,
                         String content,
                         File... files) {
        send(tos, null, null, subject, content, true, files);
    }

    /**
     * 发送 HTML 邮件给多个人（带抄送）
     *
     * @param tos     收件人列表
     * @param ccs     抄送人列表
     * @param subject 主题
     * @param content 内容
     * @param files   附件列表
     */
    public void sendHtml(Collection<String> tos,
                         Collection<String> ccs,
                         String subject,
                         String content,
                         File... files) {
        send(tos, ccs, null, subject, content, true, files);
    }

    /**
     * 发送 HTML 邮件给多个人（带抄送和密送）
     *
     * @param tos     收件人列表
     * @param ccs     抄送人列表
     * @param bccs    密送人列表
     * @param subject 主题
     * @param content 内容
     * @param files   附件列表
     */
    public void sendHtml(Collection<String> tos,
                         Collection<String> ccs,
                         Collection<String> bccs,
                         String subject,
                         String content,
                         File... files) {
        send(tos, ccs, bccs, subject, content, true, files);
    }

    public void sendTestMail(MailConfig mailConfig, String to, String subject, String content) {
        JavaMailSender tempMailSender = createMailSender(mailConfig);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(getFromAddress(mailConfig));
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(content);
        tempMailSender.send(simpleMailMessage);
    }

    /**
     * 发送邮件给多个人
     *
     * @param tos     收件人列表
     * @param ccs     抄送人列表
     * @param bccs    密送人列表
     * @param subject 主题
     * @param content 内容
     * @param isHtml  是否是 HTML
     * @param files   附件列表
     */
    public void send(Collection<String> tos,
                     Collection<String> ccs,
                     Collection<String> bccs,
                     String subject,
                     String content,
                     boolean isHtml,
                     File... files) {
        if (CollectionUtil.isEmpty(tos)) {
            throw MailException.recipientIsRequired(); // 请至少指定一名收件人
        }
        try {
            JavaMailSender mailSender = this.getMailSender();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 发信人
            MailConfig mailAccount = this.getMailConfig();
            String fromAddress = getFromAddress(mailAccount);
            messageHelper.setFrom(fromAddress);
            // 收信人
            messageHelper.setTo(tos.toArray(new String[0]));
            // 抄送人
            if (CollectionUtil.isNotEmpty(ccs)) {
                messageHelper.setCc(ccs.toArray(new String[0]));
            }
            // 密送人
            if (CollectionUtil.isNotEmpty(bccs)) {
                messageHelper.setBcc(bccs.toArray(new String[0]));
            }
            // 主题
            messageHelper.setSubject(subject);
            // 内容
            messageHelper.setText(content, isHtml);
            // 附件
            if (ArrayUtil.isNotEmpty(files)) {
                for (File file : files) {
                    messageHelper.addAttachment(file.getName(), file);
                }
            }
            // 发送邮件
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw MailException.sendingFailed(e);
        }
    }

    /**
     * 将多个联系人转为列表，分隔符为逗号或者分号
     *
     * @param addresses 多个联系人，如果为空返回空列表
     * @return 联系人列表
     */
    private List<String> splitAddress(String addresses) {
        if (StrUtil.isBlank(addresses)) {
            return new ArrayList<>(0);
        }
        List<String> result;
        if (StrUtil.contains(addresses, ",")) {
            result = StrUtil.splitTrim(addresses, ",");
        } else if (StrUtil.contains(addresses, ";")) {
            result = StrUtil.splitTrim(addresses, ";");
        } else {
            result = Collections.singletonList(addresses);
        }
        return result;
    }

    /**
     * 获取发件人地址
     */
    private String getFromAddress(MailConfig mailConfig) {
        String fromName = mailConfig.getFrom();
        String username = mailConfig.getUsername();

        if (StrUtil.isNotBlank(fromName)) {
            return fromName + " <" + username + ">";
        }
        return username;
    }


    /**
     * 动态创建 JavaMailSender
     */
    public JavaMailSender createMailSender(MailConfig mailConfig) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // 从配置中获取邮件服务器信息
        mailSender.setHost(mailConfig.getHost());
        mailSender.setPort(mailConfig.getPort());
        mailSender.setUsername(mailConfig.getUsername());
        mailSender.setPassword(mailConfig.getPassword());
        mailSender.setDefaultEncoding("UTF-8");

        // 配置邮件属性
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        if (mailConfig.isSslEnabled()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", mailConfig.getHost());
            props.put("mail.smtp.ssl.checkserveridentity", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(mailConfig.getPort()));
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        return mailSender;
    }

    /**
     * 读取最新配置，创建邮件发送器
     */
    public synchronized void reloadConfig() {
        this.mailConfig = mailConfigProvider.getMailConfig();
        this.javaMailSender = createMailSender(mailConfig);
    }

    /**
     * 获取当前邮件发送器
     */
    public JavaMailSender getMailSender() {
        if (javaMailSender == null) {
            reloadConfig();
        }
        return javaMailSender;
    }

    /**
     * 获取当前邮件账户
     */
    public MailConfig getMailConfig() {
        if (mailConfig == null) {
            reloadConfig();
        }
        return mailConfig;
    }
}
