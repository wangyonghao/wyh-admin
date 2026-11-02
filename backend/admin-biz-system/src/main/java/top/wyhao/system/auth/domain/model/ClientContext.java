package top.wyhao.system.auth.domain.model;

/**
 * 登录请求侧上下文（IP / UA），由应用层从 HTTP 组装后传入 domain。
 */
public record ClientContext(String ip, String userAgent) {

    public static ClientContext of(String ip, String userAgent) {
        return new ClientContext(ip, userAgent);
    }
}
