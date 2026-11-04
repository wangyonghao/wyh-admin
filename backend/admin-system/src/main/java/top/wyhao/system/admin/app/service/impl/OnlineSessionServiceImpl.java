package top.wyhao.system.admin.app.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.admin.app.dto.OnlineUserResult;
import top.wyhao.system.admin.app.service.OnlineSessionService;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.web.core.model.PageQuery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 在线会话查询 / 强退（Sa-Token 细节下沉）
 */
@Service
@RequiredArgsConstructor
public class OnlineSessionServiceImpl implements OnlineSessionService {

    @Override
    public PageResult<OnlineUserResult> page(String keyword, PageQuery pageQuery) {
        List<OnlineUserResult> all = listAllSessions();
        String kw = StrUtil.trimToEmpty(keyword).toLowerCase(Locale.ROOT);
        if (StrUtil.isNotBlank(kw)) {
            all = all.stream()
                    .filter(u -> containsIgnoreCase(u.getLoginName(), kw)
                            || containsIgnoreCase(u.getIp(), kw)
                            || containsIgnoreCase(u.getLocation(), kw))
                    .toList();
        }
        return PageResult.of(pageQuery.getPage(), pageQuery.getPageSize(), all);
    }

    @Override
    public void revoke(String token) {
        String currentToken = LoginUtil.getTokenValue();
        if (ObjectUtil.equal(token, currentToken)) {
            throw UserException.kickoutSelfNotAllowed();
        }
        LoginUtil.kickout(token);
    }

    @Override
    public void revokeBatch(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        String currentToken = LoginUtil.getTokenValue();
        for (String token : tokens) {
            if (StrUtil.isBlank(token) || ObjectUtil.equal(token, currentToken)) {
                continue;
            }
            LoginUtil.kickout(token);
        }
    }

    private List<OnlineUserResult> listAllSessions() {
        // size=-1：取出全部 token-session id，再在内存过滤/分页，保证 total 真实
        List<String> sessionIds = StpUtil.searchTokenSessionId("", 0, -1, false);
        List<OnlineUserResult> onlineUsers = new ArrayList<>();
        if (sessionIds == null) {
            return onlineUsers;
        }
        for (String sessionId : sessionIds) {
            try {
                SaSession session = StpUtil.getSessionBySessionId(sessionId);
                if (session == null) {
                    continue;
                }
                String token = session.getToken();
                long loginTime = session.get("loginTime", session.getCreateTime());
                long lastAccessTime = StpUtil.getStpLogic().getTokenLastActiveTime(token);
                onlineUsers.add(new OnlineUserResult(
                        sessionId,
                        token,
                        resolveLoginName(session),
                        session.get("ipaddr", ""),
                        session.get("loginLocation", ""),
                        session.get("browser", ""),
                        session.get("os", ""),
                        formatTime(loginTime),
                        formatTime(lastAccessTime)
                ));
            } catch (Exception ignored) {
                // 忽略无效 session
            }
        }
        return onlineUsers;
    }

    private static String resolveLoginName(SaSession session) {
        String loginName = session.get("loginName", "");
        if (StrUtil.isNotBlank(loginName)) {
            return loginName;
        }
        Object loginUser = session.get(LoginUtil.LOGIN_USER_KEY);
        if (loginUser instanceof LoginUser user && StrUtil.isNotBlank(user.getUsername())) {
            return user.getUsername();
        }
        return "";
    }

    private static boolean containsIgnoreCase(String value, String keywordLower) {
        return StrUtil.isNotBlank(value) && value.toLowerCase(Locale.ROOT).contains(keywordLower);
    }

    private static LocalDateTime formatTime(long timestamp) {
        if (timestamp <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}
