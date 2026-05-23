package top.wyhao.admin.cmn.mail;

public class MailException extends RuntimeException {
    public MailException(String message) {
        super(message);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

    public static MailException recipientIsRequired() {
        return new MailException("请至少指定一名收件人");
    }

    public static MailException sendingFailed(Exception e) {
        return new MailException("发送邮件失败", e);
    }

}
