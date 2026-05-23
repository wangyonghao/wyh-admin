
package top.wyhao.starter.messaging.websocket.dao;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话 DAO 默认实现
 *

 * @since 2.1.0
 */
public class WebSocketSessionDaoDefaultImpl implements WebSocketSessionDao {

    private static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void add(String key, WebSocketSession session) {
        SESSION_MAP.put(key, session);
    }

    @Override
    public void delete(String key) {
        SESSION_MAP.remove(key);
    }

    @Override
    public WebSocketSession get(String key) {
        return SESSION_MAP.get(key);
    }

    @Override
    public Collection<WebSocketSession> listAll() {
        return SESSION_MAP.values();
    }

    @Override
    public Set<String> listAllSessionIds() {
        return SESSION_MAP.keySet();
    }
}
