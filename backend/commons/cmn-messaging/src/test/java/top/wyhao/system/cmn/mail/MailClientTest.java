package top.wyhao.system.cmn.mail;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MailClientTest {

    @Test
    void sendRejectsEmptyRecipientsBeforeOpeningSmtp() {
        MailClient client = new MailClient(() -> null);

        MailException ex = assertThrows(MailException.class,
                () -> client.send(List.of(), null, null, "subject", "content", false));

        assertEquals("请至少指定一名收件人", ex.getMessage());
    }

    @Test
    void createMailSenderSkipsAuthForLocalSmtpWithoutCredentials() {
        MailConfig config = account("127.0.0.1", 2500, "", "", false);

        JavaMailSenderImpl sender = (JavaMailSenderImpl) new MailClient(() -> null).createMailSender(config);
        Properties props = sender.getJavaMailProperties();

        assertEquals("false", props.get("mail.smtp.auth"));
        assertEquals("true", props.get("mail.smtp.starttls.enable"));
        assertEquals("false", props.get("mail.smtp.starttls.required"));
    }

    @Test
    void createMailSenderEnablesAuthAndSslWhenConfigured() {
        MailConfig config = account("smtp.example", 465, "a@b.c", "secret", true);

        JavaMailSenderImpl sender = (JavaMailSenderImpl) new MailClient(() -> null).createMailSender(config);
        Properties props = sender.getJavaMailProperties();

        assertEquals("smtp.example", sender.getHost());
        assertEquals(465, sender.getPort());
        assertEquals("true", props.get("mail.smtp.auth"));
        assertEquals("true", props.get("mail.smtp.ssl.enable"));
        assertEquals("smtp.example", props.get("mail.smtp.ssl.trust"));
        assertEquals("465", props.get("mail.smtp.socketFactory.port"));
    }

    @Test
    void getMailConfigLoadsAccountFromProvider() {
        MailConfig stored = account("smtp.example", 25, "u", "p", false);
        MailClient client = new MailClient(() -> stored);

        assertEquals("smtp.example", client.getMailConfig().getHost());
        assertEquals(25, ((JavaMailSenderImpl) client.getMailSender()).getPort());
    }

    private static MailConfig account(String host, int port, String username, String password, boolean ssl) {
        MailConfig config = new MailConfig();
        config.setHost(host);
        config.setPort(port);
        config.setUsername(username);
        config.setPassword(password);
        config.setSslEnabled(ssl);
        return config;
    }
}
