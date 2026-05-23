
package top.wyhao.admin.job;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;

/**
 * 任务调度服务端启动程序
 *

 * @since 2024/6/25 22:24
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class JobApplication extends com.aizuda.snailjob.server.SnailJobServerApplication implements ApplicationRunner {

    private final ServerProperties serverProperties;

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        String hostAddress = NetUtil.getLocalhostStr();
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        String baseUrl = URLUtil.normalize("%s:%s%s".formatted(hostAddress, port, contextPath));
        log.info("----------------------------------------------");
        log.info("{} service started successfully.", SpringUtil.getApplicationName());
        log.info("访问地址：{}", baseUrl);
        log.info("在线文档：https://snailjob.opensnail.com");
        log.info("----------------------------------------------");
    }
}
