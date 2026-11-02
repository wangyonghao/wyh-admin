package top.wyhao.system.open.handler;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.dev33.satoken.router.SaRouter;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedElement;

import static cn.dev33.satoken.annotation.handler.SaCheckPermissionHandler._checkMethod;

/**
 * 重定义注解 SaCheckPermission 的处理器
 */
@Component
public class SaCheckPermissionHandler implements SaAnnotationHandlerInterface<SaCheckPermission> {

    /**
     * Open API 路径，签名已由拦截器校验，无登录态可用于权限校验
     */
    private static final String OPEN_API_PATH = "/open/**";

    @Override
    public Class<SaCheckPermission> getHandlerAnnotationClass() {
        return SaCheckPermission.class;
    }

    @Override
    public void checkMethod(SaCheckPermission saCheckPermission, AnnotatedElement annotatedElement) {
        if (!isOpenApiRequest()) {
            _checkMethod(saCheckPermission.type(), saCheckPermission.value(), saCheckPermission
                .mode(), saCheckPermission.orRole());
        }
    }

    /**
     * 判断当前请求是否为 Open API 请求
     *
     * @return 是否为 Open API 请求（true：是；false：否）
     */
    public static boolean isOpenApiRequest() {
        return SaRouter.isMatchCurrURI(OPEN_API_PATH);
    }
}
