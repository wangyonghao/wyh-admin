
package top.wyhao.starter.messaging.websocket.core;

import org.springframework.http.server.ServletServerHttpRequest;

/**
 * WebSocket 客户端服务
 *

 * @since 2.1.0
 */
public interface WebSocketClientService {

    /**
     * 获取当前客户端 ID
     *
     * @param request 请求对象
     * @return 当前客户端 ID
     */
    String getClientId(ServletServerHttpRequest request);
}
