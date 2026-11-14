package top.wyhao.system.auth.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.wyhao.system.auth.domain.exception.AuthenticationException;

import java.net.URI;

/**
 * 验证链接的公开前缀只来自 application.url，不读请求 Host。
 * 生产必须是 HTTPS。本机 http 只接受 localhost 和 127.0.0.1。
 */
@Component
public class RegistrationLinkOrigin {

    private final String applicationUrl;

    public RegistrationLinkOrigin(@Value("${application.url}") String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String requireBaseUrl() {
        String value = applicationUrl == null ? "" : applicationUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException ex) {
            throw invalid();
        }
        String host = uri.getHost();
        String scheme = uri.getScheme();
        boolean local = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
        boolean trusted = "https".equalsIgnoreCase(scheme) || ("http".equalsIgnoreCase(scheme) && local);
        if (host == null || scheme == null || uri.getUserInfo() != null || !trusted) {
            throw invalid();
        }
        return value;
    }

    private static AuthenticationException invalid() {
        return new AuthenticationException("REGISTER_PUBLIC_URL_INVALID", "注册链接地址未正确配置");
    }
}
