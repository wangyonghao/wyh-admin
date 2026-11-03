
package top.wyhao.starter.messaging.websocket.core;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import top.wyhao.starter.messaging.websocket.autoconfigure.WebSocketProperties;

import java.util.Map;

/**
 * WebSocket 拦截器
 *


 * @since 2.1.0
 */
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    private final WebSocketProperties webSocketProperties;
    private final WebSocketClientService webSocketClientService;

    public WebSocketInterceptor(WebSocketProperties webSocketProperties,
                                WebSocketClientService webSocketClientService) {
        this.webSocketProperties = webSocketProperties;
        this.webSocketClientService = webSocketClientService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String clientId = webSocketClientService.getClientId((ServletServerHttpRequest)request);
        attributes.put(webSocketProperties.getClientIdKey(), clientId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        super.afterHandshake(request, response, wsHandler, exception);
    }
}
