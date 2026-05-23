
package top.wyhao.admin.schedule.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import top.wyhao.starter.web.core.model.PageResult;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import com.aizuda.snailjob.common.core.model.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.wyhao.admin.schedule.model.exception.ScheduleClientException;
import top.wyhao.admin.schedule.model.exception.ScheduleServerException;
import top.wyhao.admin.schedule.model.JobPageResult;
import top.wyhao.starter.cache.redisson.util.RedisUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 任务调度客户端

 * @since 2024/7/4 23:07
 */
@Slf4j
@Data
public class JobClient {

    public static final String AUTH_TOKEN_HEADER = "Snail-Job-Auth";
    public static final Integer STATUS_SUCCESS = 1;
    private static final String AUTH_URL = "/auth/login";
    private final String url;
    private final String username;
    private final String password;

    public JobClient(String url, String username, String password) {
        Assert.notBlank(url, "任务调度中心 URL 不能为空");
        Assert.notBlank(username, "任务调度中心用户名不能为空");
        Assert.notBlank(password, "任务调度中心密码不能为空");
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 请求
     *
     * @param apiSupplier API 请求
     * @param <T>         响应类型
     * @return 响应信息
     */
    public <T> T request(Supplier<Result<T>> apiSupplier) {
        Result<T> result = apiSupplier.get();
        if (!STATUS_SUCCESS.equals(result.getStatus())) {
            throw new ScheduleClientException(result.getMessage());
        }
        return result.getData();
    }

    /**
     * 分页请求
     *
     * @param apiSupplier API 请求
     * @param <T>         响应类型
     * @return 分页列表信息
     */
    public <T> PageResult<T> requestPage(Supplier<JobPageResult<List<T>>> apiSupplier) {
        JobPageResult<List<T>> result = apiSupplier.get();
        if (!STATUS_SUCCESS.equals(result.getStatus())) {
            throw new ScheduleClientException(result.getMessage());
        }
        PageResult<T> page = new PageResult<>();
        page.setList(result.getData());
        page.setTotal(result.getTotal());
        return page;
    }

    /**
     * 获取 Token
     *
     * @return Token
     */
    public String getToken() {
        String token = RedisUtils.get(AUTH_TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            token = this.authenticate();
            Object expiresAtSeconds = JWTUtil.parseToken(token).getPayload(RegisteredPayload.EXPIRES_AT);
            RedisUtils.set(AUTH_TOKEN_HEADER, token, Duration.ofSeconds(Convert
                .toLong(expiresAtSeconds) - DateUtil.currentSeconds() - 60));
        }
        return token;
    }

    /**
     * 密码认证
     *
     * @return Token
     */
    private String authenticate() {
        Map<String, Object> paramMap = MapUtil.newHashMap(2);
        paramMap.put("username", username);
        paramMap.put("password", SecureUtil.md5(password));
        HttpRequest httpRequest = HttpUtil.createPost("%s%s".formatted(url, AUTH_URL));
        httpRequest.body(JSONUtil.toJsonStr(paramMap));
        try (HttpResponse response = httpRequest.execute()) {
            if (!response.isOk() || response.body() == null) {
                throw new ScheduleServerException("连接任务调度中心异常");
            }
            Result<?> result = JSONUtil.toBean(response.body(), Result.class);
            if (!STATUS_SUCCESS.equals(result.getStatus())) {
                log.warn("Password Authentication failed, expected a successful response. error msg: {}", result
                    .getMessage());
                throw new ScheduleServerException(result.getMessage());
            }
            return JSONUtil.parseObj(result.getData()).getStr("token");
        } catch (IORuntimeException e) {
            throw new ScheduleServerException("无法连接任务调度中心，请检查调度中心服务");
        }
    }
}
