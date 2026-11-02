package top.wyhao.system.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.sign.SaSignManager;
import cn.dev33.satoken.sign.template.SaSignUtil;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.wyhao.system.config.satoken.ApiSignTemplate;
import top.wyhao.cmn.core.constant.OrderedConstants;
import top.wyhao.web.config.SecurityProperties;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class ApiSignConfig implements WebMvcConfigurer {

    private final SecurityProperties properties;
    private final ApiSignTemplate apiSignTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        SaSignManager.setSaSignTemplate(apiSignTemplate);
        registry.addInterceptor(new SaInterceptor(
                handle -> {
                    SaRouter.notMatch("/open/**").check(r -> StpUtil.checkLogin());
                    // Open Api校验
                    SaRouter.match("/open/**").check(r -> SaSignUtil.checkRequest(SaHolder.getRequest()));
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(properties.getExcludes())
                .order(OrderedConstants.Interceptor.AUTH_INTERCEPTOR);
    }
}
