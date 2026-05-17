
package top.wyhao.admin;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.starter.core.autoconfigure.application.ApplicationProperties;

/**
 * 启动程序
 */
@Slf4j
@EnableAsync(proxyTargetClass = true)
@ComponentScan(basePackages = {"top.wyhao"})
@EnableFeignClients(basePackages = {"top.wyhao"})
@RestController
@SpringBootApplication
@RequiredArgsConstructor
public class AdminApplication implements ApplicationRunner {

    private final ApplicationProperties applicationProperties;
    private final ServerProperties serverProperties;

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    @Hidden
    @SaIgnore
    @GetMapping("/")
    public ApplicationProperties index() {
        return applicationProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String hostAddress = NetUtil.getLocalhostStr();
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        String baseUrl = URLUtil.normalize("%s:%s%s".formatted(hostAddress, port, contextPath));
        log.info("--------------------------------------------------------");
        log.info("{} 服务启动成功", applicationProperties.getName());
        log.info("当前版本: v{} (Profile: {})", applicationProperties.getVersion(), SpringUtil.getProperty("spring.profiles.active"));
        log.info("服务地址: {}", baseUrl);
        log.info("接口文档: {}/doc.html", baseUrl);
        log.info("--------------------------------------------------------");
    }
}