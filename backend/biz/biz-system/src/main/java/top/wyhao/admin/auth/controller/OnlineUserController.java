
package top.wyhao.admin.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.admin.auth.model.OnlineUserResult;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 在线用户 API

 * @since 2026/4/27
 */
@Tag(name = "在线用户 API")
@RestController
@RequiredArgsConstructor
public class OnlineUserController {
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("monitor:online:list")
    @GetMapping("/monitor/online")
    public PageResult<OnlineUserResult> page(@Valid String keyword, @Valid PageQuery pageQuery) {
        int start = (pageQuery.getPage() - 1) * pageQuery.getSize();

        List<String> sessionIds = StpUtil.searchTokenSessionId("", start, pageQuery.getSize(), false);

        List<OnlineUserResult> onlineUsers = new ArrayList<>();
        for (String sessionId : sessionIds) {
            try {
                SaSession session = StpUtil.getSessionBySessionId(sessionId);
                if (session != null) {
                    OnlineUserResult online = new OnlineUserResult();
                    online.setSessionId(sessionId);
                    online.setToken(session.getToken());

                    long loginTime = session.get("loginTime", session.getCreateTime());
                    online.setLoginTime(formatTime(loginTime));
                    online.setLoginName(session.get("loginName", ""));
                    online.setIp(session.get("ipaddr", ""));
                    online.setLocation(session.get("loginLocation", ""));
                    online.setBrowser(session.get("browser", ""));
                    online.setOs(session.get("os", ""));
                    long lastAccessTime =  StpUtil.getStpLogic().getTokenLastActiveTime(session.getToken());
                    online.setLastActiveTime(formatTime(lastAccessTime));
                    onlineUsers.add(online);
                }
            } catch (Exception e) {
                // 忽略无效的session
            }
        }
        return PageResult.build(pageQuery.getPage(), pageQuery.getSize(), onlineUsers);
    }

    private LocalDateTime formatTime(long timestamp) {
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                java.time.ZoneId.systemDefault()
        );
    }


    @Operation(summary = "强退在线用户", description = "强退在线用户")
    @Parameter(name = "token", description = "令牌", example = "ey****J9.ey****fQ.7q****vE", in = ParameterIn.PATH)
    @SaCheckPermission("monitor:online:kickout")
    @DeleteMapping("/monitor/online/{token}")
    public void kickout(@PathVariable String token) {
        String currentToken = LoginUtil.getTokenValue();
        BizAssert.throwIfEqual(token, currentToken, "不能强退自己");
        LoginUtil.kickout(token);
    }

    @Operation(summary = "批量强退在线用户", description = "批量强退在线用户")
    @SaCheckPermission("monitor:online:kickout")
    @DeleteMapping("/monitor/online")
    public void batchKickout(@Valid @org.springframework.web.bind.annotation.RequestBody List<String> tokens) {
        String currentToken = LoginUtil.getTokenValue();
        for (String token : tokens) {
            if (!token.equals(currentToken)) {
                LoginUtil.kickout(token);
            }
        }
    }


}
