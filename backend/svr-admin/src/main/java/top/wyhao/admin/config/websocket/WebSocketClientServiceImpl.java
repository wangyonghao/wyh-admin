
package top.wyhao.admin.config.websocket;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import top.wyhao.starter.core.exception.SystemException;
import top.wyhao.starter.messaging.websocket.core.WebSocketClientService;

/**
 * 当前登录用户 Provider
 *

 * @since 2024/6/4 22:13
 */
@Component
public class WebSocketClientServiceImpl implements WebSocketClientService {

    @Override
    public String getClientId(ServletServerHttpRequest request) {
        HttpServletRequest servletRequest = request.getServletRequest();
        String token = servletRequest.getParameter("token");
        if (StpUtil.getLoginIdByToken(token) == null) {
            throw new SystemException("登录已过期，请重新登录");
        }
        return token;
    }
}
