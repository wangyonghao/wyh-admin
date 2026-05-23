
package top.wyhao.starter.messaging.websocket.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wyhao.starter.core.constant.PropertiesConstants;
import top.wyhao.starter.core.constant.StringConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WebSocket 配置属性
 *

 * @since 2.1.0
 */
@ConfigurationProperties(PropertiesConstants.MESSAGING_WEBSOCKET)
public class WebSocketProperties {

    private static final List<String> ALL = Collections.singletonList(StringConstants.ASTERISK);

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 路径
     */
    private String path = StringConstants.SLASH + "websocket";

    /**
     * 允许跨域的域名
     */
    private List<String> allowedOrigins = new ArrayList<>(ALL);

    /**
     * 客户端 ID Key
     */
    private String clientIdKey = "CLIENT_ID";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getClientIdKey() {
        return clientIdKey;
    }

    public void setClientIdKey(String clientIdKey) {
        this.clientIdKey = clientIdKey;
    }
}
