
package top.wyhao.system.config.satoken;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.cmn.core.model.Result;
import top.wyhao.tenant.context.TenantContextHolder;
import top.wyhao.web.http.ServletUtils;
import top.wyhao.web.json.util.JSONUtils;

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
