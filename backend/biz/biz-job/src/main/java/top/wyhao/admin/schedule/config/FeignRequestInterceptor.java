
package top.wyhao.admin.schedule.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.wyhao.admin.schedule.api.JobClient;

/**
 * Feign 请求拦截器

 * @since 2025/3/28 21:17
 */
@Component
@RequiredArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

    /**
     * 请求头：命名空间 ID
     */
    private static final String NAMESPACE_ID_HEADER = "SNAIL-JOB-NAMESPACE-ID";
    private final JobClient jobClient;

    @Value("${snail-job.namespace}")
    private String namespace;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(NAMESPACE_ID_HEADER, namespace);
        requestTemplate.header(JobClient.AUTH_TOKEN_HEADER, jobClient.getToken());
    }
}