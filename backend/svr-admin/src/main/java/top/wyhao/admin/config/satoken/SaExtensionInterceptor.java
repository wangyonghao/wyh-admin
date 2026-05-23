
package top.wyhao.admin.config.satoken;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.model.Result;
import top.wyhao.starter.tenant.context.TenantContextHolder;
import top.wyhao.starter.web.http.ServletUtils;
import top.wyhao.starter.web.json.util.JSONUtils;

/**
 * Sa-Token 扩展拦截器
 *

 * @since 2024/10/10 20:25
 */
@Slf4j
public class SaExtensionInterceptor extends SaInterceptor {

    public SaExtensionInterceptor(SaParamFunction<Object> auth) {
        super(auth);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        boolean flag = super.preHandle(request, response, handler);
        if (!flag || !StpUtil.isLogin()) {
            return flag;
        }
        // 设置上下文
        LoginUser loginUser = LoginUtil.getLoginUser();
        if (loginUser == null) {
            return true;
        }
        if (!loginUser.getTenantId().equals(TenantContextHolder.getTenantId())) {
            Result<Void> result = Result.fail(String.valueOf(HttpStatus.FORBIDDEN.value()), "您当前没有访问该租户的权限");
            ServletUtils.writeJSON(response, JSONUtils.toJsonStr(result));
            return false;
        }

        return true;
    }
}
